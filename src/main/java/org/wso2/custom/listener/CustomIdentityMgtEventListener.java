package org.wso2.custom.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.mgt.policy.PolicyViolationException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.Map;

/**
 * This is an implementation of UserOperationEventListener.
 * This defines additional operations specific to custom password policy enforcement.
 */
public class CustomIdentityMgtEventListener extends AbstractIdentityUserOperationEventListener {

    private static Log log = LogFactory.getLog(CustomIdentityMgtEventListener.class);

    public CustomIdentityMgtEventListener() {

        super();
        log.info("CustomIdentityMgtEventListener instantiated...");
    }

    @Override
    public int getExecutionOrderId() {
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 93;
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            if (credential == null || StringUtils.isBlank(credential.toString())) {
                log.error("Custom Identity Management listener is disabled");
                throw new UserStoreException("invalid password.");
            }
            return true;
        }


        //invoke validatePasswordPatterns to enforce custom password policy.
        validatePasswordPatterns(userName, credential);
        return true;
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            if (newCredential == null || StringUtils.isBlank(newCredential.toString())) {
                log.error("Custom Identity Management listener is disabled");
                throw new UserStoreException("invalid password.");
            }
            return true;
        }
        //invoke validatePasswordPatterns to enforce custom password policy.
        validatePasswordPatterns(userName, newCredential);
        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            if (newCredential == null || StringUtils.isBlank(newCredential.toString())) {
                log.error("Custom Identity Management listener is disabled");
                throw new UserStoreException("invalid password.");
            }
            return true;
        }

        //invoke validatePasswordPatterns to enforce custom password policy.
        validatePasswordPatterns(userName, newCredential);
        return true;
    }


    private void validatePasswordPatterns(String userName, Object credential)
            throws UserStoreException {

        log.info("userName=" + userName);
        log.info("credential=" + credential);

            try {
                //Implement the PolicyRegistry for the extension defined in the config file.
                CustomIdentityMgtConfig.getInstance().getPolicyRegistry()
                        .enforcePasswordPolicies(credential.toString(), userName);
            } catch (PolicyViolationException e) {
                throw new UserStoreException(e.getMessage(), e);
            }

    }
}

