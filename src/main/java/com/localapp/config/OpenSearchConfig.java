package com.localapp.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

import static com.localapp.constant.Constants.OPENSEARCH_ENDPOINT;

@Configuration
public class OpenSearchConfig {
    @Bean
    public OpenSearchClient openSearchClient() {
        // Configure AWS HTTP client without credentialsProvider
        SdkHttpClient httpClient = ApacheHttpClient.builder()
                .build();

        // Create credentials provider for signing
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        // Use default transport options
        AwsSdk2TransportOptions transportOptions = AwsSdk2TransportOptions.builder()
                .build();

        // Create OpenSearch client with AwsSdk2Transport
        return new OpenSearchClient(
                new AwsSdk2Transport(
                        httpClient,
                        OPENSEARCH_ENDPOINT,
                        "es", // Service name for OpenSearch
                        Region.US_EAST_2,
                        transportOptions
                )
        );
    }
}