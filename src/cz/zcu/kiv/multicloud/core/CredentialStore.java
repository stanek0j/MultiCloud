package cz.zcu.kiv.multicloud.core;

import java.util.Map.Entry;
import java.util.Set;

import cz.zcu.kiv.multicloud.core.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.core/CredentialStore.java
 *
 * Basic interface of the credential store. It is recommended to implement a custom credential store as a singleton and pass it along to each of {@link cz.zcu.kiv.multicloud.core.oauth2.OAuth2} instances.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface CredentialStore {

	/**
	 * Retrieves all the identifiers used in the store.
	 * @return Set of all identifiers.
	 */
	Set<String> getIdentifiers();

	/**
	 * Retrieves all the tokens from the store.
	 * @return Set of all the tokens.
	 */
	Set<OAuth2Token> getTokens();

	/**
	 * Retrieves all the credentials stored in the store.
	 * @return Set of all identifier - token pairs.
	 */
	Set<Entry<String, OAuth2Token>> retrieveAllCredentials();

	/**
	 * Returns {@link cz.zcu.kiv.multicloud.core.oauth2.OAuth2Token} from the store identified by identifier.
	 * @param identifier Identifier of the token in the store.
	 * @return Returns the token, if it exists. If not, null is returned.
	 */
	OAuth2Token retrieveCredential(String identifier);

	/**
	 * Save new {@link cz.zcu.kiv.multicloud.core.oauth2.OAuth2Token} into the store.
	 * @param token Token to be saved.
	 * @return Assigned identifier.
	 */
	String storeCredential(OAuth2Token token);

	/**
	 * Update existing {@link cz.zcu.kiv.multicloud.core.oauth2.OAuth2Token} or save it with custom identifier.
	 * @param identifier Identifier of the token in the store.
	 * @param token Token to be saved.
	 */
	void storeCredential(String identifier, OAuth2Token token);

}
