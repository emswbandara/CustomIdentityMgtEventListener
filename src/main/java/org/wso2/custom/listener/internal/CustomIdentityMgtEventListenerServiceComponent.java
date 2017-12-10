package org.wso2.custom.listener.internal;

import org.wso2.custom.listener.CustomIdentityMgtConfig;
import org.wso2.custom.listener.CustomIdentityMgtEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.custom.listener.internal.CustomIdentityMgtEventListenerServiceComponent"
 * immediate=true
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class CustomIdentityMgtEventListenerServiceComponent {

    private static Log log = LogFactory.getLog(CustomIdentityMgtEventListenerServiceComponent.class);
    private static CustomIdentityMgtEventListener listener = null;

    protected void activate(ComponentContext context) {

        CustomIdentityMgtConfig.getInstance(CustomIdentityMgtEventListenerServiceDataHolder.getInstance()
                .getRealmService()
                .getBootstrapRealmConfiguration());

        //register the custom listener as an OSGI service.
        listener = new CustomIdentityMgtEventListener();
        context.getBundleContext().registerService(UserOperationEventListener.class, listener, null);

        log.info("CustomIdentityMgtEventListenerServiceComponent bundle activated successfully..");
    }

    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("CustomIdentityMgtEventListenerServiceComponent is deactivated ");
        }
    }

    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service.");
        }
        CustomIdentityMgtEventListenerServiceDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("UnSetting the Realm Service.");
        }
        CustomIdentityMgtEventListenerServiceDataHolder.getInstance().setRealmService(null);
    }

}
