/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.api.to.LoadBalancerTO.DestinationTO;
import com.cloud.agent.api.to.LoadBalancerTO.StickinessPolicyTO;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.network.rules.LbStickinessMethod.StickinessMethodType;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;


/**
 * @author chiradeep
 *
 */
public class HAProxyConfigurator implements LoadBalancerConfigurator {

    private static String[] globalSection = { "global",
            "\tlog 127.0.0.1:3914   local0 warning", "\tmaxconn 4096",
            "\tchroot /var/lib/haproxy", "\tuser haproxy", "\tgroup haproxy",
            "\tdaemon" };

    private static String[] defaultsSection = { "defaults", "\tlog     global",
            "\tmode    tcp", "\toption  dontlognull", "\tretries 3",
            "\toption redispatch", "\toption forwardfor",
            "\toption forceclose", "\ttimeout connect    5000",
            "\ttimeout client     50000", "\ttimeout server     50000" };

    private static String[] defaultListen = { "listen  vmops 0.0.0.0:9",
            "\toption transparent" };

    @Override
    public String[] generateConfiguration(List<PortForwardingRuleTO> fwRules) {
        // Group the rules by publicip:publicport
        Map<String, List<PortForwardingRuleTO>> pools = new HashMap<String, List<PortForwardingRuleTO>>();

        for (PortForwardingRuleTO rule : fwRules) {
            StringBuilder sb = new StringBuilder();
            String poolName = sb.append(rule.getSrcIp().replace(".", "_"))
                    .append('-').append(rule.getSrcPortRange()[0]).toString();
            if (!rule.revoked()) {
                List<PortForwardingRuleTO> fwList = pools.get(poolName);
                if (fwList == null) {
                    fwList = new ArrayList<PortForwardingRuleTO>();
                    pools.put(poolName, fwList);
                }
                fwList.add(rule);
            }
        }

        List<String> result = new ArrayList<String>();

        result.addAll(Arrays.asList(globalSection));
        result.add(getBlankLine());
        result.addAll(Arrays.asList(defaultsSection));
        result.add(getBlankLine());

        if (pools.isEmpty()) {
            // haproxy cannot handle empty listen / frontend or backend, so add
            // a dummy listener
            // on port 9
            result.addAll(Arrays.asList(defaultListen));
        }
        result.add(getBlankLine());

        for (Map.Entry<String, List<PortForwardingRuleTO>> e : pools.entrySet()) {
            List<String> poolRules = getRulesForPool(e.getKey(), e.getValue());
            result.addAll(poolRules);
        }

        return result.toArray(new String[result.size()]);
    }

    private List<String> getRulesForPool(String poolName,
            List<PortForwardingRuleTO> fwRules) {
        PortForwardingRuleTO firstRule = fwRules.get(0);
        String publicIP = firstRule.getSrcIp();
        String publicPort = Integer.toString(firstRule.getSrcPortRange()[0]);
        // FIXEME: String algorithm = firstRule.getAlgorithm();

        List<String> result = new ArrayList<String>();
        // add line like this: "listen  65_37_141_30-80 65.37.141.30:80"
        StringBuilder sb = new StringBuilder();
        sb.append("listen ").append(poolName).append(" ").append(publicIP)
                .append(":").append(publicPort);
        result.add(sb.toString());
        sb = new StringBuilder();
        // FIXME sb.append("\t").append("balance ").append(algorithm);
        result.add(sb.toString());
        if (publicPort.equals(NetUtils.HTTP_PORT)) {
            sb = new StringBuilder();
            sb.append("\t").append("mode http");
            result.add(sb.toString());
            sb = new StringBuilder();
            sb.append("\t").append("option httpclose");
            result.add(sb.toString());
        }
        int i = 0;
        for (PortForwardingRuleTO rule : fwRules) {
            // add line like this: "server  65_37_141_30-80_3 10.1.1.4:80 check"
            if (rule.revoked()) {
                continue;
            }
            sb = new StringBuilder();
            sb.append("\t").append("server ").append(poolName).append("_")
                    .append(Integer.toString(i++)).append(" ")
                    .append(rule.getDstIp()).append(":")
                    .append(rule.getDstPortRange()[0]).append(" check");
            result.add(sb.toString());
        }
        result.add(getBlankLine());
        return result;
    }
/*
 * cookie <name> [ rewrite | insert | prefix ] [ indirect ] [ nocache ]
              [ postonly ] [ preserve ] [ domain <domain> ]*
              [ maxidle <idle> ] [ maxlife <life> ]
  Enable cookie-based persistence in a backend.
  May be used in sections :   defaults | frontend | listen | backend
                                 yes   |    no    |   yes  |   yes
  Arguments :
    <name>    is the name of the cookie which will be monitored, modified or
              inserted in order to bring persistence. This cookie is sent to
              the client via a "Set-Cookie" header in the response, and is
              brought back by the client in a "Cookie" header in all requests.
              Special care should be taken to choose a name which does not
              conflict with any likely application cookie. Also, if the same
              backends are subject to be used by the same clients (eg:
              HTTP/HTTPS), care should be taken to use different cookie names
              between all backends if persistence between them is not desired.

    rewrite   This keyword indicates that the cookie will be provided by the
              server and that haproxy will have to modify its value to set the
              server's identifier in it. This mode is handy when the management
              of complex combinations of "Set-cookie" and "Cache-control"
              headers is left to the application. The application can then
              decide whether or not it is appropriate to emit a persistence
              cookie. Since all responses should be monitored, this mode only
              works in HTTP close mode. Unless the application behaviour is
              very complex and/or broken, it is advised not to start with this
              mode for new deployments. This keyword is incompatible with
              "insert" and "prefix".

    insert    This keyword indicates that the persistence cookie will have to
              be inserted by haproxy in server responses if the client did not

              already have a cookie that would have permitted it to access this
              server. When used without the "preserve" option, if the server
              emits a cookie with the same name, it will be remove before
              processing.  For this reason, this mode can be used to upgrade
              existing configurations running in the "rewrite" mode. The cookie
              will only be a session cookie and will not be stored on the
              client's disk. By default, unless the "indirect" option is added,
              the server will see the cookies emitted by the client. Due to
              caching effects, it is generally wise to add the "nocache" or
              "postonly" keywords (see below). The "insert" keyword is not
              compatible with "rewrite" and "prefix".

    prefix    This keyword indicates that instead of relying on a dedicated
              cookie for the persistence, an existing one will be completed.
              This may be needed in some specific environments where the client
              does not support more than one single cookie and the application
              already needs it. In this case, whenever the server sets a cookie
              named <name>, it will be prefixed with the server's identifier
              and a delimiter. The prefix will be removed from all client
              requests so that the server still finds the cookie it emitted.
              Since all requests and responses are subject to being modified,
              this mode requires the HTTP close mode. The "prefix" keyword is
              not compatible with "rewrite" and "insert".

    indirect  When this option is specified, no cookie will be emitted to a
              client which already has a valid one for the server which has
              processed the request. If the server sets such a cookie itself,
              it will be removed, unless the "preserve" option is also set. In
              "insert" mode, this will additionally remove cookies from the
              requests transmitted to the server, making the persistence
              mechanism totally transparent from an application point of view.

    nocache   This option is recommended in conjunction with the insert mode
              when there is a cache between the client and HAProxy, as it
              ensures that a cacheable response will be tagged non-cacheable if
              a cookie needs to be inserted. This is important because if all
              persistence cookies are added on a cacheable home page for
              instance, then all customers will then fetch the page from an
              outer cache and will all share the same persistence cookie,
              leading to one server receiving much more traffic than others.
              See also the "insert" and "postonly" options.

    postonly  This option ensures that cookie insertion will only be performed
              on responses to POST requests. It is an alternative to the
              "nocache" option, because POST responses are not cacheable, so
              this ensures that the persistence cookie will never get cached.
              Since most sites do not need any sort of persistence before the
              first POST which generally is a login request, this is a very
              efficient method to optimize caching without risking to find a
              persistence cookie in the cache.
              See also the "insert" and "nocache" options.

    preserve  This option may only be used with "insert" and/or "indirect". It
              allows the server to emit the persistence cookie itself. In this
              case, if a cookie is found in the response, haproxy will leave it
              untouched. This is useful in order to end persistence after a
              logout request for instance. For this, the server just has to
              emit a cookie with an invalid value (eg: empty) or with a date in
              the past. By combining this mechanism with the "disable-on-404"
              check option, it is possible to perform a completely graceful
              shutdown because users will definitely leave the server after
              they logout.

    domain    This option allows to specify the domain at which a cookie is
              inserted. It requires exactly one parameter: a valid domain
              name. If the domain begins with a dot, the browser is allowed to
              use it for any host ending with that name. It is also possible to
              specify several domain names by invoking this option multiple
              times. Some browsers might have small limits on the number of
              domains, so be careful when doing that. For the record, sending
              10 domains to MSIE 6 or Firefox 2 works as expected.

    maxidle   This option allows inserted cookies to be ignored after some idle
              time. It only works with insert-mode cookies. When a cookie is
              sent to the client, the date this cookie was emitted is sent too.
              Upon further presentations of this cookie, if the date is older
              than the delay indicated by the parameter (in seconds), it will
              be ignored. Otherwise, it will be refreshed if needed when the
              response is sent to the client. This is particularly useful to
              prevent users who never close their browsers from remaining for
              too long on the same server (eg: after a farm size change). When
              this option is set and a cookie has no date, it is always
              accepted, but gets refreshed in the response. This maintains the
              ability for admins to access their sites. Cookies that have a
              date in the future further than 24 hours are ignored. Doing so
              lets admins fix timezone issues without risking kicking users off
              the site.

    maxlife   This option allows inserted cookies to be ignored after some life
              time, whether they're in use or not. It only works with insert
              mode cookies. When a cookie is first sent to the client, the date
              this cookie was emitted is sent too. Upon further presentations
              of this cookie, if the date is older than the delay indicated by
              the parameter (in seconds), it will be ignored. If the cookie in
              the request has no date, it is accepted and a date will be set.
              Cookies that have a date in the future further than 24 hours are
              ignored. Doing so lets admins fix timezone issues without risking
              kicking users off the site. Contrary to maxidle, this value is
              not refreshed, only the first visit date counts. Both maxidle and
              maxlife may be used at the time. This is particularly useful to
              prevent users who never close their browsers from remaining for
              too long on the same server (eg: after a farm size change). This
              is stronger than the maxidle method in that it forces a
              redispatch after some absolute delay.

  There can be only one persistence cookie per HTTP backend, and it can be
  declared in a defaults section. The value of the cookie will be the value
  indicated after the "cookie" keyword in a "server" statement. If no cookie
  is declared for a given server, the cookie is not set.

  Examples :
        cookie JSESSIONID prefix
        cookie SRV insert indirect nocache
        cookie SRV insert postonly indirect
        cookie SRV insert indirect nocache maxidle 30m maxlife 8h
 */
    private String getLbSubRuleForStickiness(LoadBalancerTO lbTO) throws Exception{
        int i = 0;
        
        if (lbTO.getStickinessPolicies() == null)
            return null;
        
        StringBuilder sb = new StringBuilder();

        for (StickinessPolicyTO stickinessPolicy : lbTO.getStickinessPolicies()) {
            if (stickinessPolicy == null)
                continue;
            Map<String, String> paramsList = stickinessPolicy.getParams();
            i++;
            
            /*
             *  * cookie <name> [ rewrite | insert | prefix ] [ indirect ] [ nocache ]
              [ postonly ] [ preserve ] [ domain <domain> ]*
              [ maxidle <idle> ] [ maxlife <life> ]
             */
            if (StickinessMethodType.LBCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                /* Default Values */
                String name = null; /* required */
                String mode ="insert "; /* optional*/
                Boolean indirect = false; /* optional*/
                Boolean nocache = false; /* optional*/
                Boolean postonly = false; /* optional*/
                Boolean preserve = false; /* optional*/
                String domain = null; /* optional*/
                String maxidle = null; /* optional*/
                String maxlife = null; /* optional*/
                

                Iterator it = paramsList.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = (Map.Entry) it.next();
                    if ("name".equalsIgnoreCase(pairs.getKey())) name = pairs.getValue();
                    if ("mode".equalsIgnoreCase(pairs.getKey())) mode = pairs.getValue();
                    if ("domain".equalsIgnoreCase(pairs.getKey())) domain = pairs.getValue();
                    if ("maxidle".equalsIgnoreCase(pairs.getKey())) maxidle = pairs.getValue();
                    if ("maxlife".equalsIgnoreCase(pairs.getKey())) maxlife = pairs.getValue();
                        
                }
                if (name == null)  {/* re-check all mandatory params */
                    /*
                     * Not supposed to reach here, validation of params are
                     * done at the higher layer
                     */
                    throw new Exception("Haproxy: name is mandatory \n");
                }
                sb.append("\t").append("cookie ").append(name).append(" ").append(mode).append(" ");
                if (indirect) sb.append("indirect ");
                if (nocache) sb.append("nocache ");
                if (postonly) sb.append("postonly ");
                if (indirect) sb.append("indirect ");
                if (preserve) sb.append("preserve ");
                if (domain != null) sb.append("domain ").append(domain).append(" ");
                if (maxidle != null) sb.append("maxidle ").append(maxidle).append(" ");
                if (maxlife != null) sb.append("maxlife ").append(maxlife).append(" ");
                        
            } else if (StickinessMethodType.SourceBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                /* Default Values */
                String tablesize = "200k"; /* optional */
                String expire = "30m"; /* optional */

                /* overwrite default values with the stick parameters */
                Iterator it = paramsList.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = (Map.Entry) it.next();
                    if ("tablesize".equalsIgnoreCase(pairs.getKey()))
                        tablesize = pairs.getValue();
                    if ("expire".equalsIgnoreCase(pairs.getKey()))
                        expire = pairs.getValue();
                }

                sb.append("\t").append("stick-table type ip size ")
                        .append(tablesize).append(" expire ").append(expire);
                sb.append("\n\t").append("stick on src");
            } else if (StickinessMethodType.AppCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                /*
                 * FORMAT : appsession <cookie> len <length> timeout <holdtime>
                 * [request-learn] [prefix] [mode
                 * <path-parameters|query-string>]
                 */
                /* example: appsession JSESSIONID len 52 timeout 3h */
                String cookiename = null; /* required */
                String length = null; /* required */
                String holdtime = null; /* required */
                String mode = null; /* optional */
                Boolean requestlearn = false; /* optional */
                Boolean prefix = false; /* optional */

                Iterator it = paramsList.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = (Map.Entry) it.next();
                    if ("cookiename".equalsIgnoreCase(pairs.getKey()))   cookiename = pairs.getValue();
                    if ("length".equalsIgnoreCase(pairs.getKey()))    length = pairs.getValue();                   
                    if ("holdtime".equalsIgnoreCase(pairs.getKey())) holdtime = pairs.getValue();       
                    if ("mode".equalsIgnoreCase(pairs.getKey()))      mode = pairs.getValue();
                }
                if ((cookiename == null) || (length == null) || (holdtime == null)) {
                    /*
                     * Not supposed to reach here, validation of params are
                     * done at the higher layer
                     */
                    throw new Exception("Haproxy: cookiename/length/holdtime are mandatory params\n");
                }
                sb.append("\t").append("appsession ").append(cookiename)
                        .append(" len ").append(length).append(" timeout ")
                        .append(holdtime).append(" ");
                if (prefix) sb.append("prefix ");
                if (requestlearn) sb.append("request-learn").append(mode).append(" ");
                if (mode != null) sb.append("mode ").append(mode).append(" ");

            } else {
                /*
                 * Not supposed to reach here, validation of methods are
                 * done at the higher layer
                 */
                throw new Exception("Haproxy: Not supported Method\n");
            }
        }
        if (i == 0) return null;
        return sb.toString();
    }

    private List<String> getRulesForPool(LoadBalancerTO lbTO) throws Exception {
        StringBuilder sb = new StringBuilder();
        String poolName = sb.append(lbTO.getSrcIp().replace(".", "_"))
                .append('-').append(lbTO.getSrcPort()).toString();
        String publicIP = lbTO.getSrcIp();
        String publicPort = Integer.toString(lbTO.getSrcPort());
        String algorithm = lbTO.getAlgorithm();

        List<String> result = new ArrayList<String>();
        // add line like this: "listen  65_37_141_30-80 65.37.141.30:80"
        sb = new StringBuilder();
        sb.append("listen ").append(poolName).append(" ").append(publicIP)
                .append(":").append(publicPort);
        result.add(sb.toString());
        sb = new StringBuilder();
        sb.append("\t").append("balance ").append(algorithm);
        result.add(sb.toString());

        String stickinessSubRule = getLbSubRuleForStickiness(lbTO);
        if (stickinessSubRule != null)
            result.add(stickinessSubRule);

        if (publicPort.equals(NetUtils.HTTP_PORT)) {
            sb = new StringBuilder();
            sb.append("\t").append("mode http");
            result.add(sb.toString());
            sb = new StringBuilder();
            sb.append("\t").append("option httpclose");
            result.add(sb.toString());
        }
        int i = 0;
        for (DestinationTO dest : lbTO.getDestinations()) {
            // add line like this: "server  65_37_141_30-80_3 10.1.1.4:80 check"
            if (dest.isRevoked()) {
                continue;
            }
            sb = new StringBuilder();
            sb.append("\t").append("server ").append(poolName).append("_")
                    .append(Integer.toString(i++)).append(" ")
                    .append(dest.getDestIp()).append(":")
                    .append(dest.getDestPort()).append(" check");
            result.add(sb.toString());
        }
        result.add(getBlankLine());
        return result;
    }

    private String getBlankLine() {
        return new String("\t ");
    }

    private String generateStatsRule(LoadBalancerConfigCommand lbCmd,
            String ruleName, String statsIp) {
        StringBuilder rule = new StringBuilder("\nlisten ").append(ruleName)
                .append(" ").append(statsIp).append(":")
                .append(lbCmd.lbStatsPort);
        rule.append(
                "\n\tmode http\n\toption httpclose\n\tstats enable\n\tstats uri     ")
                .append(lbCmd.lbStatsUri)
                .append("\n\tstats realm   Haproxy\\ Statistics\n\tstats auth    ")
                .append(lbCmd.lbStatsAuth);
        rule.append("\n");
        return rule.toString();
    }

    @Override
    public String[] generateConfiguration(LoadBalancerConfigCommand lbCmd) throws Exception  {
        List<String> result = new ArrayList<String>();

        result.addAll(Arrays.asList(globalSection));
        result.add(getBlankLine());
        result.addAll(Arrays.asList(defaultsSection));
        if (!lbCmd.lbStatsVisibility.equals("disabled")) {
            /* new rule : listen admin_page guestip/link-local:8081 */
            if (lbCmd.lbStatsVisibility.equals("global")) {
                result.add(generateStatsRule(lbCmd, "stats_on_public",
                        lbCmd.lbStatsPublicIP));
            } else if (lbCmd.lbStatsVisibility.equals("guest-network")) {
                result.add(generateStatsRule(lbCmd, "stats_on_guest",
                        lbCmd.lbStatsGuestIP));
            } else if (lbCmd.lbStatsVisibility.equals("link-local")) {
                result.add(generateStatsRule(lbCmd, "stats_on_private",
                        lbCmd.lbStatsPrivateIP));
            } else if (lbCmd.lbStatsVisibility.equals("all")) {
                result.add(generateStatsRule(lbCmd, "stats_on_public",
                        lbCmd.lbStatsPublicIP));
                result.add(generateStatsRule(lbCmd, "stats_on_guest",
                        lbCmd.lbStatsGuestIP));
                result.add(generateStatsRule(lbCmd, "stats_on_private",
                        lbCmd.lbStatsPrivateIP));
            } else {
                /*
                 * stats will be available on the default http serving port, no
                 * special stats port
                 */
                StringBuilder subRule = new StringBuilder(
                        "\tstats enable\n\tstats uri     ")
                        .append(lbCmd.lbStatsUri)
                        .append("\n\tstats realm   Haproxy\\ Statistics\n\tstats auth    ")
                        .append(lbCmd.lbStatsAuth);
                result.add(subRule.toString());
            }

        }
        result.add(getBlankLine());

        if (lbCmd.getLoadBalancers().length == 0) {
            // haproxy cannot handle empty listen / frontend or backend, so add
            // a dummy listener
            // on port 9
            result.addAll(Arrays.asList(defaultListen));
        }
        result.add(getBlankLine());

        for (LoadBalancerTO lbTO : lbCmd.getLoadBalancers()) {
            List<String> poolRules;
            try {
                poolRules = getRulesForPool(lbTO);
                result.addAll(poolRules);
            } catch (Exception e) {
                throw  e;
            }
        }

        return result.toArray(new String[result.size()]);
    }

    @Override
    public String[][] generateFwRules(LoadBalancerConfigCommand lbCmd) {
        String[][] result = new String[3][];
        Set<String> toAdd = new HashSet<String>();
        Set<String> toRemove = new HashSet<String>();
        Set<String> toStats = new HashSet<String>();

        for (LoadBalancerTO lbTO : lbCmd.getLoadBalancers()) {

            StringBuilder sb = new StringBuilder();
            sb.append(lbTO.getSrcIp()).append(":");
            sb.append(lbTO.getSrcPort()).append(":");
            String lbRuleEntry = sb.toString();
            if (!lbTO.isRevoked()) {
                toAdd.add(lbRuleEntry);
            } else {
                toRemove.add(lbRuleEntry);
            }
        }
        StringBuilder sb = new StringBuilder("");
        if (lbCmd.lbStatsVisibility.equals("guest-network")) {
            sb = new StringBuilder(lbCmd.lbStatsGuestIP).append(":")
                    .append(lbCmd.lbStatsPort).append(":")
                    .append(lbCmd.lbStatsSrcCidrs).append(":,");
        } else if (lbCmd.lbStatsVisibility.equals("link-local")) {
            sb = new StringBuilder(lbCmd.lbStatsPrivateIP).append(":")
                    .append(lbCmd.lbStatsPort).append(":")
                    .append(lbCmd.lbStatsSrcCidrs).append(":,");
        } else if (lbCmd.lbStatsVisibility.equals("global")) {
            sb = new StringBuilder(lbCmd.lbStatsPublicIP).append(":")
                    .append(lbCmd.lbStatsPort).append(":")
                    .append(lbCmd.lbStatsSrcCidrs).append(":,");
        } else if (lbCmd.lbStatsVisibility.equals("all")) {
            sb = new StringBuilder("0.0.0.0/0").append(":")
                    .append(lbCmd.lbStatsPort).append(":")
                    .append(lbCmd.lbStatsSrcCidrs).append(":,");
        }
        toStats.add(sb.toString());

        toRemove.removeAll(toAdd);
        result[ADD] = toAdd.toArray(new String[toAdd.size()]);
        result[REMOVE] = toRemove.toArray(new String[toRemove.size()]);
        result[STATS] = toStats.toArray(new String[toStats.size()]);

        return result;
    }
}
