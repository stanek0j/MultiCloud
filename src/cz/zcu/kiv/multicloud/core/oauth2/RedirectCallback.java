package cz.zcu.kiv.multicloud.core.oauth2;

import java.util.Map;

public interface RedirectCallback {

	WebPage onRedirect(Map<String, String> response);
	
}
