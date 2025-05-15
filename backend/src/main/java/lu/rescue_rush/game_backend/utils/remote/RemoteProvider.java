package lu.rescue_rush.game_backend.utils.remote;

import java.util.Objects;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Service
public class RemoteProvider {

	private static final Logger LOGGER = Logger.getLogger(RemoteProvider.class.getName());

	@Value("${remote.url}")
	private String remoteUrl;

	@Autowired
	private RestTemplate restTemplate;

	@PostConstruct
	private void init() {
		if (!remoteUrl.endsWith("/")) {
			remoteUrl += "/";
		}
		LOGGER.info("Remote URL: " + remoteUrl);
	}

	@Cacheable(value = "remoteResource", key = "#url", unless = "#result == null")
	public String fetchRemoteData(String url) {
		Objects.requireNonNull(url, "URL cannot be null");
		if (url.startsWith("/")) {
			url = url.substring(1);
		}
		LOGGER.info("Fetching resource: " + (remoteUrl + url));
		return restTemplate.getForObject(remoteUrl + url, String.class);
	}

}
