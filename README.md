# CI/CD Pipeline – Java Maven Application Deployment Using Jenkins

## 1. Introduction

This document provides a hands-on demonstration of how to automate the build, test, and deployment of a **Java Maven application** using **Jenkins**.

In this demo:

- Developer pushes Java code to Git.
- Jenkins automatically detects the code change.
- Jenkins checks out the source code.
- Maven builds the application.
- Unit tests are executed.
- A JAR file is generated.
- Jenkins deploys the JAR file to an Ubuntu server.
- The Java application is restarted automatically.

### CI/CD Flow

```text
Developer
    |
    | git push
    v
Git Repository
    |
    | Webhook
    v
Jenkins
    |
    +--> Checkout
    |
    +--> Maven Build
    |
    +--> Unit Test
    |
    +--> Package JAR
    |
    +--> Deploy JAR
    |
    +--> Restart Application
    |
    v
Ubuntu Server
    |
    v
Java Application
```

---

# 2. Training Objectives

By the end of this session, developers will understand:

- What CI/CD is
- How Jenkins works
- How Jenkins integrates with Git
- How Maven builds a Java application
- How to create a Jenkins Pipeline
- How to use a `Jenkinsfile`
- How to generate a JAR file
- How to deploy a JAR automatically
- How to restart a Java application automatically
- How to configure Jenkins credentials
- How to troubleshoot a failed deployment

---

# 3. Prerequisites

## Developer Machine

The developer should have:

```text
Git
Java
Maven
IDE
Access to Git repository
```

Verify:

```bash
java -version
mvn -version
git --version
```

---

# 4. Jenkins Server

The Jenkins server is running on Ubuntu.

Example:

```text
Ubuntu Server
      |
      +-- Jenkins
      +-- Java
      +-- Maven
      +-- Git
```

Verify the tools on the Jenkins server:

```bash
java -version
mvn -version
git --version
```

---

# 5. Target Deployment Server

The target server is also Ubuntu.

Example deployment location:

```text
/opt/apps/cicd-demo/
```

The deployed JAR will be:

```text
/opt/apps/cicd-demo/cicd-demo.jar
```

Application:

```text
Java Application
     |
     v
cicd-demo.jar
     |
     v
Java Process
```

---

# 6. Maven Project Structure

The application should follow a standard Maven project structure.

```text
cicd-demo/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/example/
│   │           └── Application.java
│   │
│   └── test/
│       └── java/
│           └── com/example/
│               └── ApplicationTest.java
│
├── pom.xml
├── Jenkinsfile
└── README.md
```

---

# 7. Maven Build

The `pom.xml` contains the project configuration.

Example:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
         http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>cicd-demo</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.0</version>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.4.2</version>
            </plugin>
        </plugins>
    </build>

</project>
```

Adjust the Java version and Maven configuration according to the application.

---

# 8. Build the Application Manually

Before introducing Jenkins, demonstrate the Maven build manually.

From the project directory:

```bash
mvn clean package
```

Maven performs:

```text
Clean
  ↓
Compile
  ↓
Test
  ↓
Package
```

The JAR will normally be generated under:

```text
target/
```

Example:

```text
target/cicd-demo-1.0.0.jar
```

Verify:

```bash
ls -lh target/
```

---

# 9. Run the JAR Manually

Before automating deployment, demonstrate how the application is started manually.

Example:

```bash
java -jar target/cicd-demo-1.0.0.jar
```

For a background process:

```bash
nohup java -jar target/cicd-demo-1.0.0.jar > app.log 2>&1 &
```

Check the process:

```bash
ps -ef | grep cicd-demo
```

Check the log:

```bash
tail -f app.log
```

---

# 10. Deployment Directory

On the target Ubuntu server, create the application directory:

```bash
sudo mkdir -p /opt/apps/cicd-demo
```

Set appropriate ownership:

```bash
sudo chown -R <application-user>:<application-user> /opt/apps/cicd-demo
```

Example:

```text
/opt/apps/cicd-demo/
```

The final deployment should look like:

```text
/opt/apps/cicd-demo/
│
├── cicd-demo.jar
└── app.log
```

---

# 11. Manual JAR Deployment

Before Jenkins automation, demonstrate the manual process.

```text
Developer Machine
       |
       | Build
       v
target/cicd-demo-1.0.0.jar
       |
       | Copy
       v
Ubuntu Server
       |
       v
/opt/apps/cicd-demo/cicd-demo.jar
       |
       | Restart
       v
Java Application
```

Example using SCP:

```bash
scp target/cicd-demo-1.0.0.jar \
    <user>@<server>:/opt/apps/cicd-demo/cicd-demo.jar
```

Then SSH to the server:

```bash
ssh <user>@<server>
```

Stop the old application:

```bash
pkill -f cicd-demo.jar || true
```

Start the new application:

```bash
nohup java -jar /opt/apps/cicd-demo/cicd-demo.jar \
    > /opt/apps/cicd-demo/app.log 2>&1 &
```

Check:

```bash
ps -ef | grep cicd-demo
```

---

# 12. Why Automate This?

Without Jenkins:

```text
Developer
   |
   v
Build manually
   |
   v
Copy JAR manually
   |
   v
SSH to server
   |
   v
Stop application
   |
   v
Start application
```

This process is:

- Manual
- Time-consuming
- Error-prone
- Difficult to repeat consistently

With Jenkins:

```text
Git Push
   |
   v
Jenkins
   |
   +--> Build
   |
   +--> Test
   |
   +--> Package
   |
   +--> Copy JAR
   |
   +--> Restart Application
```

---

# 13. Jenkins Pipeline

The pipeline will contain the following stages:

```text
Checkout
   ↓
Build
   ↓
Test
   ↓
Package
   ↓
Deploy
   ↓
Restart
   ↓
Verify
```

---

# 14. Jenkinsfile

Create a file named:

```text
Jenkinsfile
```

in the root of the Maven project.

Example:

```groovy
pipeline {

    agent any

    tools {
        maven 'Maven'
        jdk 'JDK17'
    }

    environment {
        APP_NAME = 'cicd-demo'
        DEPLOY_DIR = '/opt/apps/cicd-demo'
        JAR_NAME = 'cicd-demo.jar'
        SERVER = '<DEPLOYMENT_SERVER>'
        USER = '<DEPLOYMENT_USER>'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building Maven application...'
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                echo 'Creating JAR file...'
                sh 'mvn clean package -DskipTests'

                sh '''
                    ls -lh target/
                '''
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying JAR to Ubuntu server...'

                sh """
                    scp target/*.jar \
                    ${USER}@${SERVER}:${DEPLOY_DIR}/${JAR_NAME}
                """
            }
        }

        stage('Restart Application') {
            steps {
                echo 'Restarting application...'

                sh """
                    ssh ${USER}@${SERVER} '
                        pkill -f ${JAR_NAME} || true

                        nohup java -jar \
                        ${DEPLOY_DIR}/${JAR_NAME} \
                        > ${DEPLOY_DIR}/app.log 2>&1 &
                    '
                """
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'Verifying deployment...'

                sh """
                    ssh ${USER}@${SERVER} '
                        ps -ef | grep ${JAR_NAME} | grep -v grep
                    '
                """
            }
        }
    }

    post {

        success {
            echo 'Deployment completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Please check the Jenkins console logs.'
        }
    }
}
```

> **Trainer note:** The SSH/SCP commands above are intentionally simple for a training demo. For a production pipeline, use Jenkins-managed SSH credentials and preferably a `systemd` service rather than `pkill` + `nohup`.

---

# 15. Jenkins Tool Configuration

Go to:

```text
Manage Jenkins
    ↓
Tools
```

Configure:

### JDK

Example:

```text
Name: JDK17
JAVA_HOME: /path/to/java
```

### Maven

Example:

```text
Name: Maven
MAVEN_HOME: /path/to/maven
```

The names must match the names used in the Jenkinsfile:

```groovy
tools {
    maven 'Maven'
    jdk 'JDK17'
}
```

---

# 16. Configure SSH Access

Jenkins needs permission to connect to the deployment server.

The recommended approach is SSH key authentication.

Architecture:

```text
Jenkins Server
      |
      | SSH Key
      v
Ubuntu Deployment Server
```

Test connectivity from the Jenkins environment:

```bash
ssh <deployment-user>@<deployment-server>
```

Also test:

```bash
scp test.jar \
<deployment-user>@<deployment-server>:/opt/apps/cicd-demo/
```

The important point is:

> Do not wait until the Jenkins pipeline to discover that SSH access is not working.

Test the connection first.

---

# 17. Jenkins Credentials

Do not put private keys or passwords directly inside the `Jenkinsfile`.

Configure credentials in Jenkins:

```text
Manage Jenkins
    ↓
Credentials
    ↓
Global
    ↓
Add Credentials
```

For SSH deployment, use:

```text
Kind: SSH Username with private key
```

Example credential ID:

```text
deployment-server-ssh
```

Then use the credential in the Jenkins pipeline.

A more secure deployment stage can use:

```groovy
stage('Deploy') {
    steps {
        sshagent(['deployment-server-ssh']) {

            sh """
                scp target/*.jar \
                ${USER}@${SERVER}:${DEPLOY_DIR}/${JAR_NAME}
            """
        }
    }
}
```

And the restart stage:

```groovy
stage('Restart Application') {
    steps {
        sshagent(['deployment-server-ssh']) {

            sh """
                ssh ${USER}@${SERVER} '
                    pkill -f ${JAR_NAME} || true

                    nohup java -jar \
                    ${DEPLOY_DIR}/${JAR_NAME} \
                    > ${DEPLOY_DIR}/app.log 2>&1 &
                '
            """
        }
    }
}
```

---

# 18. Create Jenkins Pipeline Job

In Jenkins:

```text
New Item
```

Enter:

```text
cicd-demo
```

Select:

```text
Pipeline
```

Configure:

```text
Pipeline
    ↓
Definition
    ↓
Pipeline script from SCM
```

Select:

```text
Git
```

Enter the repository URL.

Example:

```text
<YOUR-GIT-REPOSITORY>
```

Branch:

```text
*/main
```

Script Path:

```text
Jenkinsfile
```

Save the job.

---

# 19. First Jenkins Build

Click:

```text
Build Now
```

Jenkins should execute:

```text
[Pipeline] Checkout
        ✓

[Pipeline] Build
        ✓

[Pipeline] Test
        ✓

[Pipeline] Package
        ✓

[Pipeline] Deploy
        ✓

[Pipeline] Restart Application
        ✓

[Pipeline] Verify Deployment
        ✓
```

The final result should be:

```text
SUCCESS
```

---

# 20. Verify the JAR on the Server

SSH into the deployment server:

```bash
ssh <deployment-user>@<deployment-server>
```

Check the deployment directory:

```bash
ls -lh /opt/apps/cicd-demo/
```

Expected:

```text
cicd-demo.jar
app.log
```

Check the application process:

```bash
ps -ef | grep cicd-demo.jar
```

Check logs:

```bash
tail -f /opt/apps/cicd-demo/app.log
```

---

# 21. Automatic Deployment

Once the manual Jenkins build is working, configure automatic triggering.

The desired workflow is:

```text
Developer
    |
    | git push
    v
Git Repository
    |
    | Webhook
    v
Jenkins
    |
    v
Checkout
    |
    v
Maven Build
    |
    v
Unit Test
    |
    v
Create JAR
    |
    v
Copy JAR
    |
    v
Restart Application
    |
    v
Verify
```

The developer should only need to:

```bash
git add .
git commit -m "Update application"
git push
```

Jenkins performs the remaining steps automatically.

---

# 22. Important Pipeline Rule

The most important rule for CI/CD is:

```text
If Build/Test fails
        ↓
STOP PIPELINE
        ↓
DO NOT DEPLOY
```

Example:

```text
Checkout       ✓
Build          ✓
Test           ✗
Package        -
Deploy         -
Restart        -
```

This prevents broken code from being deployed.

---

# 23. Demonstration Exercise

## Exercise 1 – Successful Deployment

Ask developers to:

1. Clone the repository.
2. Modify a Java class.
3. Commit the change.
4. Push the change.
5. Open Jenkins.
6. Start the pipeline.
7. Monitor each stage.
8. Verify the JAR on the Ubuntu server.
9. Verify the application.

---

## Exercise 2 – Test Failure

Intentionally introduce a failing unit test.

Push the code.

Expected result:

```text
Checkout       ✓
Build          ✓
Test           ✗
Package        -
Deploy         -
Restart        -
```

Discuss:

> Why didn't Jenkins deploy the application?

Answer:

Because the pipeline should not deploy an application when the validation stage has failed.

---

## Exercise 3 – Deployment Failure

Temporarily provide an incorrect deployment server or deployment directory.

Run the pipeline.

Observe:

```text
Build          ✓
Test           ✓
Package        ✓
Deploy         ✗
```

Ask developers to inspect the Jenkins console output and identify the failure.

---

# 24. Production Improvement – systemd

For the training demo, we can use:

```bash
nohup java -jar ...
```

However, a better approach on Ubuntu is to manage the application using `systemd`.

Example service:

```text
/etc/systemd/system/cicd-demo.service
```

Example:

```ini
[Unit]
Description=CI/CD Demo Java Application
After=network.target

[Service]
User=appuser
WorkingDirectory=/opt/apps/cicd-demo
ExecStart=/usr/bin/java -jar /opt/apps/cicd-demo/cicd-demo.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable the service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable cicd-demo
```

Start:

```bash
sudo systemctl start cicd-demo
```

Check status:

```bash
sudo systemctl status cicd-demo
```

Restart:

```bash
sudo systemctl restart cicd-demo
```

View logs:

```bash
journalctl -u cicd-demo -f
```

Then the Jenkins deployment becomes simpler:

```text
Copy JAR
   ↓
systemctl restart cicd-demo
   ↓
Verify
```

---

# 25. Recommended Production Pipeline

Once developers understand the basic demo, explain how this can evolve into a production-grade pipeline:

```text
                    Git
                     |
                     v
                  Jenkins
                     |
             +-------+-------+
             |               |
           Build            Test
             |               |
             +-------+-------+
                     |
                     v
              Code Quality
                     |
                     v
              Security Scan
                     |
                     v
               Package JAR
                     |
                     v
              Deploy to DEV
                     |
                     v
              Smoke Testing
                     |
                     v
             QA / Approval
                     |
                     v
             Deploy to PROD
                     |
                     v
                Monitoring
```

---

# 26. Rollback Concept

One important limitation of simply replacing:

```text
cicd-demo.jar
```

is that rollback becomes difficult.

A better deployment structure is:

```text
/opt/apps/cicd-demo/
│
├── releases/
│   ├── 1.0.0/
│   │   └── cicd-demo.jar
│   │
│   ├── 1.0.1/
│   │   └── cicd-demo.jar
│   │
│   └── 1.0.2/
│       └── cicd-demo.jar
│
└── current -> releases/1.0.2
```

Then rollback can point the application back to:

```text
releases/1.0.1
```

This is a good topic for an advanced CI/CD session.

---

# 27. Troubleshooting

## Maven command not found

```bash
mvn -version
```

Check Jenkins Maven configuration.

---

## Java version mismatch

```bash
java -version
```

Verify the JDK configured in Jenkins.

---

## JAR not generated

Check:

```bash
ls -lh target/
```

Review the Maven build logs.

---

## SSH connection failure

Test manually:

```bash
ssh <user>@<server>
```

Check:

- Jenkins credentials
- SSH key
- Username
- Server IP/hostname
- SSH port
- Firewall
- File permissions

---

## SCP permission denied

Check:

```bash
ls -ld /opt/apps/cicd-demo
```

Make sure the deployment user has write access.

---

## Application does not start

Check:

```bash
tail -100 /opt/apps/cicd-demo/app.log
```

If using systemd:

```bash
systemctl status cicd-demo
```

and:

```bash
journalctl -u cicd-demo -n 100
```

---

# 28. Key Takeaways for Developers

The main concept of this training is:

```text
Developer writes code
        ↓
Git Push
        ↓
Jenkins Pipeline
        ↓
Maven Build
        ↓
Unit Tests
        ↓
JAR Creation
        ↓
JAR Deployment
        ↓
Application Restart
        ↓
Deployment Verification
```

Jenkins is not replacing the developer's build process. It is **automating the repeatable process** so that the same steps happen consistently every time.

---

# 29. Final Architecture

```text
                    Developer
                        |
                        | git push
                        v
                 Git Repository
                        |
                        | Webhook
                        v
                +----------------+
                |     Jenkins    |
                |    Ubuntu      |
                +----------------+
                        |
                        v
                   Checkout
                        |
                        v
                  Maven Build
                        |
                        v
                   Unit Test
                        |
                        v
                  Package JAR
                        |
                        v
                SSH / SCP Deploy
                        |
                        v
              +-------------------+
              | Ubuntu App Server |
              |                   |
              | /opt/apps/        |
              |   cicd-demo/      |
              |       |           |
              |       v           |
              |  cicd-demo.jar    |
              +-------------------+
                        |
                        v
                Restart Service
                        |
                        v
                  Java Application
```

## Trainer Summary

The demo should be presented in **three phases**:

### Phase 1 – Manual

```text
mvn clean package
       ↓
Copy JAR
       ↓
SSH
       ↓
Start Java Application
```

### Phase 2 – Jenkins Manual Trigger

```text
Jenkins Build
       ↓
Maven Build
       ↓
Test
       ↓
JAR
       ↓
Deploy
       ↓
Restart
```

### Phase 3 – Fully Automated CI/CD

```text
git push
   ↓
Webhook
   ↓
Jenkins
   ↓
Build
   ↓
Test
   ↓
Package
   ↓
Deploy
   ↓
Restart
   ↓
Verify
```

**The key message for developers:**

> **"You push the code. Jenkins builds, tests, packages, and deploys it automatically."**
