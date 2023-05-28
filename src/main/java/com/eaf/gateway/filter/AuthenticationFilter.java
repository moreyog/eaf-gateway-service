package com.eaf.gateway.filter;

import com.eaf.gateway.exception.AccessDeniedException;
import com.eaf.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private RestTemplate template;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                //header contains token or not
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new AccessDeniedException("missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {

                    //Approach 1 - call identity server
                    String baseUrl = serviceUrl("IDENTITY-SERVICE");
                    String endpoint = "/auth/validate";

                    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
                    queryParams.add("token", authHeader);

                    String requestUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                            .path(endpoint)
                            .queryParams(queryParams)
                            .toUriString();

                    ResponseEntity<String> response = template.exchange(requestUrl, HttpMethod.GET, null, String.class);
                    System.out.println("valid access...!");

                    //Approach 2
                    //jwtUtil.validateToken(authHeader);

                }  catch (HttpClientErrorException.Forbidden e) {
                    System.err.println("Forbidden request: " + e.getMessage());
                    throw new AccessDeniedException("Forbidden request");
                } catch (Exception e) {
                    System.out.println("invalid access...!");
                    throw new RuntimeException("un authorized access to application");
                }
            }
            return chain.filter(exchange);
        });
    }

    @Autowired
    private DiscoveryClient discoveryClient;

    public String serviceUrl(String svcName) {
        List<ServiceInstance> list = discoveryClient.getInstances(svcName);
        if (list != null && list.size() > 0 ) {
            return list.get(0).getUri().toString();
        }
        return null;
    }

    public static class Config {

    }
}
