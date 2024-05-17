pipeline {
    agent any
    
    environment {
        SCANNER_HOME=tool 'sonar-scanner'
        NEXUS_CREDENTIALS_ID = 'deploymentRepo'
    }

    stages {
        stage('clean workspace'){
            steps{
                cleanWs()
            }
        }
        stage('Checkout from Git'){
            steps{
                git branch: 'main', url: 'https://github.com/dalimedmrad/achat'
            }
        }
        stage('MVN clean & compile'){
            steps{
                sh 'mvn clean'
                sh 'mvn package'
                sh 'mvn install'
            }
        }
        stage("Sonarqube Analysis "){
            steps{
                withSonarQubeEnv('sonar-server') {
                    sh '''$SCANNER_HOME/bin/sonar-scanner \
                          -Dsonar.projectKey=Mohamed_ali_mrad_2ALINFO12 \
                          -Dsonar.sources=. \
                          -Dsonar.host.url=http://192.168.50.4:9000 \
                          -Dsonar.login=sqp_de1b17c3ad2751e2a01853cc900bd16d6c890b32 \
                          -Dsonar.java.binaries=target/classes'''
                }
            }
        }
         stage('Deploy to Nexus') {
            steps {
                script {
                    // Utilisation des identifiants Nexus
                    withCredentials([usernamePassword(credentialsId: env.NEXUS_CREDENTIALS_ID, usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh 'mvn deploy -DskipTests=true -Dmaven.repo.local=.m2/repository'
                    }
                }
            }
        }
        stage("Docker Build & MySQL Start") {
            steps {
                script {
                    // Vérifier si le service MySQL est déjà en cours d'exécution
                    def isMySQLRunning = sh(script: 'docker ps -q -f name=mysqldb', returnStatus: true)
                    
                    if (isMySQLRunning == 0) {
                        echo "MySQL service is already running"
                    } else {
                        // Démarrer le service MySQL en utilisant Docker Compose
                        sh "docker-compose up -d mysqldb"
                        
                        // Attendre quelques secondes pour que MySQL démarre complètement
                        sleep(time: 30, unit: 'SECONDS')
                    }

                   // Construire et pousser l'image Docker
                    def timestamp = new Date().format("yyyyMMdd-HHmmss")
                    def imageTag = "achat-app:${timestamp}"
                    def registryImageTag = "dalimrd/achat-app:${timestamp}"
                    withDockerRegistry(credentialsId: 'docker', toolName: 'docker') {
                        sh "docker build -t ${imageTag} ."
                        sh "docker tag ${imageTag} ${registryImageTag}"
                        sh "trivy image ${registryImageTag} > trivyimage.txt --scanners vuln"
                        sh "docker push ${registryImageTag}"
                    }
                    
                    // Arrêter et supprimer l'ancien conteneur Docker
                    sh 'docker-compose stop api_service || true'
                    sh 'docker-compose rm -f api_service || true'
                    
                    // Lancer le nouveau conteneur Docker pour le service API
                    sh "docker-compose up -d api_service"

                }
            }
        }

    }
    }
