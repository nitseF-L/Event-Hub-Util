package com.function.util;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

/**
 * Provides a TokenCredential for use with Event Hub clients.
 * Handles either managed identity or client secret authentication.
 */
public class AzureCredentialProvider {

    public static TokenCredential get(String tenantId, String clientId, String clientSecret, boolean useManagedIdentity) {
        if (useManagedIdentity) {
            return new DefaultAzureCredentialBuilder().build();
        }

        return new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
    }
} 

