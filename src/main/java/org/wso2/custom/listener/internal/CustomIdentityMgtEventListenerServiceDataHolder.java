package org.wso2.custom.listener.internal;

import org.wso2.carbon.user.core.service.RealmService;


public class CustomIdentityMgtEventListenerServiceDataHolder {


    private static RealmService realmService;
    private static volatile CustomIdentityMgtEventListenerServiceDataHolder dataHolder;

    private CustomIdentityMgtEventListenerServiceDataHolder() {

    }

    public static CustomIdentityMgtEventListenerServiceDataHolder getInstance() {

        if (dataHolder == null) {

            synchronized (CustomIdentityMgtEventListenerServiceDataHolder.class) {
                if (dataHolder == null) {
                    dataHolder = new CustomIdentityMgtEventListenerServiceDataHolder();
                }
            }
        }
        return dataHolder;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    public RealmService getRealmService() {

        return realmService;
    }
}
