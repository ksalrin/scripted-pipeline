properties([
    parameters([
        booleanParam(defaultValue: true, description: 'Do you want to run terrform apply', name: 'terraform_apply'),
        booleanParam(defaultValue: false, description: 'Do you want to run terrform destroy', name: 'terraform_destroy'),
        choice(choices: ['dev', 'qa', 'prod'], description: '', name: 'environment'),
        string(defaultValue: '', description: 'Provide AMI ID', name: 'ami_id', trim: false)
    ])
])
def aws_region_var = ''
if(params.environment == "dev"){
    aws_region_var = "us-east-1"
}
else if(params.environment == "qa"){
    aws_region_var = "us-east-2"
}
else if(params.environment == "prod"){
    aws_region_var = "us-west-2"
}
def tf_vars = """
    s3_bucket = \"terraform-state-april-class-kellysalrin\"
    s3_folder_project = \"terraform_ec2\"
    s3_folder_region = \"us-east-1\"
    s3_folder_type = \"class\"
    s3_tfstate_file = \"infrastructure.tfstate\"
    environment = \"${params.environment}\"
    region      = \"${aws_region_var}\"
    public_key  = \"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCpqRgwDFbxA83BJgACCQhl/d1fj+++VlQE0jIMNOlhVope6PyeaBx/QgVObqLS+Q3UAOfydcySk8mV77lb0J4EZ11OAfO3fG2hFYckT0taxAdDBGnUvBxG9A21J8/phsip2jSe8jvFTGSEJtiOYjh+PD9D0sKbYA91JQg2azA7+gueQXjj4ie0C9VXMRNSYnpDHp/3Z90hxkr1bYPrhMUgN9+vzuk5hVyKGKJjinEkgxg64X2dOAbWBLMACNlHWaw5wGrJ+FXC2T0IKcRW5s6E7Lh//9Rtya1dhGuuiTafuY8ZPvgSyu7jSGcDUAxkzhs7Qwwhh1cWz8pGkbkOkaIBFyUeU8kerONO98jxqbNA1uslIBDah/W1GsZ7vKKtYcKQoSj1Ok8Q0ZNWdrnc9LhU7Q5RxTrhs+IVZDXLS+fxiP3Vk2aojSsOked8gOX+GxR7SWYGWMkjhU7bgKEPvABf206HyZ+2/vSARjdQqf0Wjc4G/TgSQmuMnby+/JF2FbM= Kelly.Salrin@KSALRIN-E7470\"
    ami_id      = \"${params.ami_id}\"
"""
node{
    stage("Pull Repo"){
        cleanWs()
        git url: 'https://github.com/ksalrin/terraform-ec2.git'
    }
    withCredentials([usernamePassword(credentialsId: 'jenkins-aws-access-key', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        withEnv(["AWS_REGION=${aws_region_var}"]) {
            stage("Terrraform Init"){
                writeFile file: "${params.environment}.tfvars", text: "${tf_vars}"
                sh """
                    bash setenv.sh ${environment}.tfvars
                    terraform-0.13 init
                """
            }        
            if (terraform_apply.toBoolean()) {
                stage("Terraform Apply"){
                    sh """
                        terraform-0.13 apply -var-file ${environment}.tfvars -auto-approve
                    """
                }
            }
            else if (terraform_destroy.toBoolean()) {
                stage("Terraform Destroy"){
                    sh """
                        terraform-0.13 destroy -var-file ${environment}.tfvars -auto-approve
                    """
                }
            }
            else {
                stage("Terraform Plan"){
                    sh """
                        terraform-0.13 plan -var-file ${environment}.tfvars
                    """
                }
            }
        }        
    }    
}