node ('master') {
    stage('Init'){
        withCredentials([sshUserPrivateKey(credentialsId: 'root', keyFileVariable: 'SSHKEY', passphraseVariable: '', usernameVariable: 'SSHUSERNAME')]) {
            sh 'ssh -i $SSHKEY root@104.131.103.232 yum install epel-release -y'
        }  
    }
}

