package org.wso2.custom.listener;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.policy.AbstractPasswordPolicyEnforcer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomPolicyEnforcer extends AbstractPasswordPolicyEnforcer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPolicyEnforcer.class);

    private String accountTypeKey;
    private int minLength;
    private int maxLength;
    private Pattern accountNamePattern;
    private Pattern passwordPattern;
    private String errorMsg;
    private String errorMsgPatternDesc;

    @Override public boolean enforce(Object... objects) {
        if (objects != null) {
            String password = objects[0].toString();
            String userName = objects[1].toString();

            if (password.length() < minLength) {
                if (StringUtils.isEmpty(errorMsg)) {
                    errorMessage = String.format("Password should at least have %s characters", minLength);
                } else {
                    errorMessage = String.format("%s Password should at least have %s characters", errorMsg, minLength);
                }
                return false;
            } else if (password.length() > maxLength) {
                if (StringUtils.isEmpty(errorMsg)) {
                    errorMessage = String.format("%s Password cannot have more than %s characters", errorMsg, maxLength);
                } else {
                    errorMessage = String.format("Password cannot have more than %s characters", maxLength);
                }
                return false;
            } else {
                Matcher matcher = passwordPattern.matcher(password);
                if (matcher.matches()) {
                    return true;
                } else {
                    if (StringUtils.isEmpty(errorMsg)) {
                        errorMessage = String.format("Password pattern policy violated. Policy: %s", passwordPattern);
                    } else {
                        errorMessage = String.format("%s %s", errorMsg, errorMsgPatternDesc);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override public void init(Map<String, String> map) {
        String namePatternString = null;
        String passwordPatternString = null;

        if (!MapUtils.isEmpty(map)) {
            try {
                minLength = Integer.parseInt(map.get("min.length"));
            } catch (NumberFormatException nfe) {
                LOGGER.error("Unable to parse the value of the min.length parameter to an integer. Using default " +
                        "value instead.", nfe);
            }
            try {
                maxLength = Integer.parseInt(map.get("max.length"));
            } catch (NumberFormatException nfe) {
                LOGGER.error("Unable to parse the value of the max.length parameter to an integer. Using default " +
                        "value instead.", nfe);
            }
            namePatternString = map.get("namePattern");
            passwordPatternString = map.get("pattern");
            errorMsg = map.get("errorMsg");
            errorMsgPatternDesc = map.get("errorMsg.patternDesc");
        }
        accountNamePattern = namePatternString == null ? null : Pattern.compile( namePatternString );
        passwordPattern = passwordPatternString == null ? null : Pattern.compile(passwordPatternString);
    }

}
