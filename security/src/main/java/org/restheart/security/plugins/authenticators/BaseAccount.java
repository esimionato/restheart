/*-
 * ========================LICENSE_START=================================
 * restheart-security
 * %%
 * Copyright (C) 2018 - 2020 SoftInstigate
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */
package org.restheart.security.plugins.authenticators;

import com.google.common.collect.Sets;
import io.undertow.security.idm.Account;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base concrete Account implementation
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
public class BaseAccount implements Account {
    private static final long serialVersionUID = 4199620709967413442L;
    final private Principal principal;
    final private LinkedHashSet<String> roles;

    /**
     *
     * @param name
     * @param roles
     */
    public BaseAccount(final String name, final Set<String> roles) {
        if (name == null) {
            throw new IllegalArgumentException("argument principal cannot be null");
        }

        if (roles == null || roles.isEmpty()) {
            this.roles = Sets.newLinkedHashSet();
        } else {
            this.roles = Sets.newLinkedHashSet(roles);
        }

        this.principal = new BasePrincipal(name);
    }

    /**
     *
     * @return
     */
    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "username="
                .concat(principal != null ? principal.getName() : "null")
                .concat(" roles=")
                .concat(roles != null ? roles.toString() : "null");
    }
}
