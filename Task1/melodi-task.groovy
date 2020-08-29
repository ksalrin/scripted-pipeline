properties([
    parameters([string(defaultValue: '', description: 'Please enter VM IP', name: 'nodeIP', trim: true),
    string(defaultValue: '', description: 'Please enter the version', name: 'branch', trim: true)
    ])
    ])

if (nodeIP?.trim()) {
    node {
        withCredentials([sshUserPrivateKey(credentialsId: 'root', keyFileVariable: 'SSHKEY', passphraseVariable: '', usernameVariable: 'SSHUSERNAME')]) {
            stage('Pull Repo') {
             git branch: '${branch}', changelog: false, poll: false, url: 'https://github.com/ikambarov/melodi.git'
            }
            stage("Install Apache") {
                sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} yum install httpd -y'
            }
            stage("Start Apache"){
             sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} systemctl start httpd'
            }
            stage("Enable Apache"){
             sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} systemctl enable httpd'
            }
            stage("Copy Files"){
             sh 'scp -r -o StrictHostKeyChecking=no -i $SSHKEY * $SSHUSERNAME@${nodeIP}:/var/www/html/'
            }
            stage("Change Ownership"){
             sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} chown -R apache:apache /var/www/html'
            }
            stage("Clear Workspace"){
             cleanWs()
            }
            stage("Slack Message"){
             slackSend channel: 'apr_devop_2020', message: 'Melodi and Apache are up and running'
            }
        }
    }    
}
else {
    error 'Please enter valid IP address'
}