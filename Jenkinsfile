pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Project Files') {
            steps {
                sh '''
                    echo "Checking project structure..."
                    test -d backend
                    test -d ecommerce-frontend
                    test -f docker-compose.yml
                    echo "Project files found successfully."
                '''
            }
        }

        stage('Backend Build') {
            steps {
                dir('backend/ecommerce-backend') {
                    sh '''
                        if [ -f mvnw ]; then
                            chmod +x mvnw
                            ./mvnw -B -DskipTests package
                        elif [ -f pom.xml ]; then
                            mvn -B -DskipTests package
                        else
                            echo "ERROR: Maven pom.xml or mvnw not found in backend/"
                            exit 1
                        fi
                    '''
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('ecommerce-frontend') {
                    sh '''
                        npm ci
                        npm run build
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Backend and frontend builds completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the console output.'
        }
    }
}
