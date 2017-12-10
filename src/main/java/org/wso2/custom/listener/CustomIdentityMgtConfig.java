package org.wso2.custom.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.NotificationSendingModule;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.policy.PolicyEnforcer;
import org.wso2.carbon.identity.mgt.policy.PolicyRegistry;
import org.wso2.carbon.user.api.RealmConfiguration;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read custom policy validator specific data.
 */
public class CustomIdentityMgtConfig {

    private static final Log log = LogFactory.getLog(CustomIdentityMgtConfig.class);
    private static CustomIdentityMgtConfig customIdentityMgtConfig;
    private List<NotificationSendingModule> sendingModules = new ArrayList<NotificationSendingModule>();
    private PolicyRegistry policyRegistry = new PolicyRegistry();

    protected Properties properties = new Properties();

    /**
     * Define the pattern of the configuration file. Assume following pattern in config.
     * Eg. Password.policy.extensions.1.min.length=6
     */
    private Pattern propertyPattern = Pattern.compile("(\\.\\d\\.)");

    public CustomIdentityMgtConfig(RealmConfiguration configuration) {

        InputStream inStream = null;

        File pipConfigXml = new File(IdentityUtil.getIdentityConfigDirPath(), IdentityMgtConstants.PropertyConfig
                .CONFIG_FILE_NAME);
        if (pipConfigXml.exists()) {
            try {
                inStream = new FileInputStream(pipConfigXml);
                properties.load(inStream);
            } catch (FileNotFoundException e) {
                log.error("Can not load identity-mgt properties file ", e);
            } catch (IOException e) {
                log.error("Can not load identity-mgt properties file ", e);
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                        log.error("Error while closing stream ", e);
                    }
                }
            }
        }

        try {
            // Load the configuration for Password.policy.extensions.
            loadPolicyExtensions(properties, IdentityMgtConstants.PropertyConfig.PASSWORD_POLICY_EXTENSIONS,
                    policyRegistry);


        } catch (Exception e) {
            log.error("Error while loading identity mgt configurations", e);
        }
    }

    /**
     * Gets instance
     * <p/>
     * As this is only called in start up syn and null check is not needed
     *
     * @param configuration a primary <code>RealmConfiguration</code>
     * @return <code>CustomIdentityMgtConfig</code>
     */
    public static CustomIdentityMgtConfig getInstance(RealmConfiguration configuration) {

        customIdentityMgtConfig = new CustomIdentityMgtConfig(configuration);
        return customIdentityMgtConfig;
    }

    public static CustomIdentityMgtConfig getInstance() {

        return customIdentityMgtConfig;
    }

    public List<NotificationSendingModule> getNotificationSendingModules() {

        return sendingModules;
    }

    public PolicyRegistry getPolicyRegistry() {

        return policyRegistry;
    }

    private void loadPolicyExtensions(Properties properties, String extensionType, PolicyRegistry policyRegistry) {

        Set<Integer> count = new HashSet();
        Iterator<String> keyValues = properties.stringPropertyNames().iterator();
        while (keyValues.hasNext()) {
            String currentProp = keyValues.next();
            if (currentProp.startsWith(extensionType)) {
                String extensionNumber = currentProp.replaceFirst(extensionType + ".", "");
                if (StringUtils.isNumeric(extensionNumber)) {
                    count.add(Integer.parseInt(extensionNumber));
                }
            }
        }
        //setting the number of extensionTypes as the upper bound as there can be many extension policy numbers,
        //eg: Password.policy.extensions.1, Password.policy.extensions.4, Password.policy.extensions.15
        Iterator<Integer> countIterator = count.iterator();
        while (countIterator.hasNext()) {
            Integer extensionIndex = countIterator.next();
            String className = properties.getProperty(extensionType + "." + extensionIndex);
            if (className == null) {
                continue;
            }
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);

                PolicyEnforcer policy = (PolicyEnforcer) clazz.newInstance();
                policy.init(getParameters(properties, extensionType, extensionIndex));
                policyRegistry.addPolicy(policy);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SecurityException e) {
                log.error("Error while loading password policies " + className, e);
            }
        }

    }

    /**
     * This utility method is used to get the parameters from the configuration
     * file for a given policy extension.
     *
     * @param prop         - properties
     * @param extensionKey - extension key which is defined in the
     *                     IdentityMgtConstants
     * @param sequence     - property sequence number in the file
     * @return Map of parameters with key and value from the configuration file.
     */
    private Map<String, String> getParameters(Properties prop, String extensionKey, int sequence) {

        Set<String> keys = prop.stringPropertyNames();

        Map<String, String> keyValues = new HashMap<String, String>();

        for (String key : keys) {
            // Get only the provided extensions.
            // Eg.Password.policy.extensions.1
            String regex = extensionKey + "." + String.valueOf(sequence);

            if (key.contains(regex)) {

                Matcher m = propertyPattern.matcher(key);

                // Find the .1. pattern in the property key.
                if (m.find()) {
                    int searchIndex = m.end();

					/*
                     * Key length is > matched pattern's end index if it has
					 * parameters
					 * in the config file.
					 */
                    if (key.length() > searchIndex) {
                        String propKey = key.substring(searchIndex);
                        String propValue = prop.getProperty(key);
                        keyValues.put(propKey, propValue);
                    }
                }
            }
        }

        return keyValues;
    }

    public Properties getProperties() {

        return properties;
    }

    public String getProperty(String key) {

        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {

        this.properties.setProperty(key, value);
    }
}


