package cz.zcu.kiv.multicloud.core.oauth2;

import java.util.Map.Entry;
import java.util.Set;

/**
 * cz.zcu.kiv.multicloud.core.oauth2/CredentialStore.java
 *
 * Basic interface of the credential store. It is recommended to implement a custom credential store as a singleton and pass it along to each of {@link cz.zcu.kiv.multicloud.core.oauth2.OAuth2} instances.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface CredentialStore {

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
	 * @return Returns false if identifier is already taken, otherwise return true on success.
	 */
	boolean storeCredential(String identifier, OAuth2Token token);
	
	/**
	 * Returns {@link cz.zcu.kiv.multicloud.core.oauth2.OAuth2Token} from the store identified by identifier.
	 * @param identifier Identifier of the token in the store.
	 * @return Returns the token, if it exists. If not, null is returned.
	 */
	OAuth2Token retrieveCredential(String identifier);
	
	/**
	 * Retrieves all the credentials stored in the store.
	 * @return Set of all identifier - token pairs.
	 */
	Set<Entry<String, OAuth2Token>> retrieveAllCredentials();
	
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

}
