package com.social.core.gateway;

import com.amazonaws.services.servicediscovery.AWSServiceDiscovery;
import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryClientBuilder;
import com.amazonaws.services.servicediscovery.model.DiscoverInstancesRequest;
import com.amazonaws.services.servicediscovery.model.DiscoverInstancesResult;
import com.amazonaws.services.servicediscovery.model.HealthStatus;
import com.amazonaws.services.servicediscovery.model.HttpInstanceSummary;
import com.social.core.gateway.config.ClientsServicesProperties;
import com.social.core.gateway.config.mudels.ClientCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CloudMapServiceLocationResolver implements ServiceLocationResolver {

    private static final String AWS_INSTANCE_IPV_4_ATTRIBUTE = "AWS_INSTANCE_IPV4";
    private static final String AWS_INSTANCE_PORT_ATTRIBUTE = "AWS_INSTANCE_PORT";

    private static final Random RAND = new Random(System.currentTimeMillis());


    @Autowired
    private ClientsServicesProperties clientsServicesProperties;


    public CloudMapServiceLocationResolver() {
        log.info("ServiceLocationResolver: {}", this.getClass().getCanonicalName());
    }

    @Override
    public String resolve(String ServiceName) {
        ClientCredential clientCredential = clientsServicesProperties.getClients().get(ServiceName);
        if(clientCredential == null){
            log.error("clientCredential not found for service {}",ServiceName);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"SERVICE_NAME_NOT_FOUND");
        }

        if(clientsServicesProperties.getCloudmap().isEnabled() == false){
            return clientCredential.getFullHost();
        }

        final AWSServiceDiscovery awsServiceDiscovery = AWSServiceDiscoveryClientBuilder.defaultClient();
        final DiscoverInstancesRequest discoverInstancesRequest = new DiscoverInstancesRequest();

        discoverInstancesRequest.setNamespaceName(clientsServicesProperties.getCloudmap().getNamespace());
        discoverInstancesRequest.setServiceName(clientCredential.getServiceName());
        discoverInstancesRequest.setHealthStatus(HealthStatus.HEALTHY.name());

        final DiscoverInstancesResult discoverInstancesResult = awsServiceDiscovery.discoverInstances(discoverInstancesRequest);

        final List<HttpInstanceSummary> allInstances = discoverInstancesResult.getInstances();

        if (log.isDebugEnabled()) {
            final List<String> serviceEndpoints = allInstances.stream().map(result -> result.getAttributes().get(AWS_INSTANCE_IPV_4_ATTRIBUTE) + ":" + result.getAttributes().get(AWS_INSTANCE_PORT_ATTRIBUTE)).collect(Collectors.toList());
            log.info("Found instances: {}", serviceEndpoints);
        }

        final HttpInstanceSummary result = allInstances.get(RAND.nextInt(allInstances.size()));
        final String serviceLocation = result.getAttributes().get(AWS_INSTANCE_IPV_4_ATTRIBUTE);

        log.info("Given {}, found {}", clientCredential.getServiceName() + "." + clientsServicesProperties.getCloudmap().getNamespace(), serviceLocation);

        return clientCredential.getProtocol() +serviceLocation+":" + clientCredential.getPort();
    }
}