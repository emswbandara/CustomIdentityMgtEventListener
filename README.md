# CustomIdentityMgtEventListener
A Custom Identity Management Event Listener to enforce custom password policies prior to user create/update operations in 
WSO2 Identity server.
This provides the ability to enforce custom password policies when the IdentityMgtEventListener for new governance features
is enabled (IdentityMgtEventListener with order id 95).

**Deploying and Configuring the Custom Password Validator**

Do the following to deploy and enforce the custom password policy in the WSO2 Identity Server.
1. Compile the custom password policy code and get the resulting .jar file.

Command: mvn clean install

2. Copy the .jar file into the <IS_HOME>/repository/components/dropins folder.

3. In the identity-mgt.properties make the policy configurations. Sample configuration would be as follows.
Password.policy.extensions.1=org.wso2.custom.listener.CustomPolicyEnforcer
Password.policy.extensions.1.min.length=6
Password.policy.extensions.1.max.length=12
Password.policy.extensions.1.pattern=^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])).{0,100}$
Password.policy.extensions.1.errorMsg='Password pattern policy violated. Password should contain a digit[0-9], a lower case letter[a-z], an upper case letter[A-Z], one of !@#$%&* characters' 

4. In the identity.xml file add the following entry under <EventListeners\>

<EventListener type="org.wso2.carbon.user.core.listener.UserOperationEventListener"
                       name="org.wso2.custom.listener.CustomIdentityMgtEventListener"
                       orderId="93" enable="true"/\>
                       
 5. Restart the server.                      
