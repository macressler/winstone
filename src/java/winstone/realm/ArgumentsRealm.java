/*
 * Copyright 2003-2006 Rick Knowles <winstone-devel at lists sourceforge net>
 * Distributed under the terms of either:
 * - the common development and distribution license (CDDL), v1.0; or
 * - the GNU Lesser General Public License, v2.1 or later
 */
package winstone.realm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import winstone.AuthenticationPrincipal;
import winstone.AuthenticationRealm;
import winstone.Logger;
import winstone.cmdline.Option;
import winstone.WebAppConfiguration;
import winstone.WinstoneResourceBundle;

/**
 * Base class for authentication realms. Subclasses provide the source of
 * authentication roles, usernames, passwords, etc, and when asked for
 * validation respond with a role if valid, or null otherwise.
 * 
 * @author mailto: <a href="rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ArgumentsRealm.java,v 1.4 2007/06/01 15:55:41 rickknowles Exp $
 */
public class ArgumentsRealm implements AuthenticationRealm {
    private static final WinstoneResourceBundle REALM_RESOURCES = new WinstoneResourceBundle("winstone.realm.LocalStrings");

    private Map passwords;
    private Map roles;

    /**
     * Constructor - this sets up an authentication realm, using the arguments
     * supplied on the command line as a source of userNames/passwords/roles.
     */
    public ArgumentsRealm(Set rolesAllowed, Map args) {
        this.passwords = new Hashtable();
        this.roles = new Hashtable();

        for (Object o : args.keySet()) {
            String key = (String) o;
            if (key.startsWith(Option.ARGUMENTS_REALM_PASSWORD.name)) {
                String userName = key.substring(Option.ARGUMENTS_REALM_PASSWORD.name.length());
                String password = (String) args.get(key);

                String roleList = WebAppConfiguration.stringArg(args, Option.ARGUMENTS_REALM_ROLES.name + userName, "");
                if (roleList.equals("")) {
                    Logger.log(Logger.WARNING, REALM_RESOURCES, "ArgumentsRealm.UndeclaredRoles", userName);
                } else {
                    StringTokenizer st = new StringTokenizer(roleList, ",");
                    List rl = new ArrayList();
                    for (; st.hasMoreTokens(); ) {
                        String currentRole = st.nextToken();
                        if (rolesAllowed.contains(currentRole))
                            rl.add(currentRole);
                    }
                    Object roleArray[] = rl.toArray();
                    Arrays.sort(roleArray);
                    this.roles.put(userName, Arrays.asList(roleArray));
                }
                this.passwords.put(userName, password);
            }
        }

        Logger.log(Logger.DEBUG, REALM_RESOURCES, "ArgumentsRealm.Initialised",
                "" + this.passwords.size());
    }

    /**
     * Authenticate the user - do we know them ? Return a principal once we know
     * them
     */
    public AuthenticationPrincipal authenticateByUsernamePassword(
            String userName, String password) {
        if ((userName == null) || (password == null))
            return null;

        String realPassword = (String) this.passwords.get(userName);
        if (realPassword == null)
            return null;
        else if (!realPassword.equals(password))
            return null;
        else
            return new AuthenticationPrincipal(userName, password,
                    (List) this.roles.get(userName));
    }

    /**
     * Retrieve an authenticated user
     */
    public AuthenticationPrincipal retrieveUser(String userName) {
        if (userName == null)
            return null;
        else
            return new AuthenticationPrincipal(userName,
                    (String) this.passwords.get(userName), (List) this.roles
                            .get(userName));
    }
}