node {
    stage("Pull Repo"){ 
         git branch: 'solution', changelog: false, poll: false, url: 'https://github.com/ikambarov/terraform-task.git'
    }
    stage("Terraform Init"){ 
        ssh '''
        cd sandbox/
        terraform init
        '''
    }
    stage("Terraform Apply"){ 

    }
}