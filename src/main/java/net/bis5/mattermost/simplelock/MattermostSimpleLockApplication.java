package net.bis5.mattermost.simplelock;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.bis5.mattermost.model.CommandRequest;
import net.bis5.mattermost.model.CommandResponse;
import net.bis5.mattermost.model.CommandResponseType;
import net.bis5.mattermost.model.PostAction;
import net.bis5.mattermost.model.PostAction.PostActionIntegration;
import net.bis5.mattermost.model.PostAction.PostActionIntegrationRequest;
import net.bis5.mattermost.model.PostAction.PostActionIntegrationResponse;
import net.bis5.mattermost.model.SlackAttachment;

@RestController
@SpringBootApplication
@Slf4j
public class MattermostSimpleLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(MattermostSimpleLockApplication.class, args);
	}

	@Autowired
	RedisTemplate<String, String> redisTemplate;
	@Autowired
	AppConfig config;

	@RequestMapping("/ping")
	public ResponseEntity<String> ping() {
		return ResponseEntity.ok("pong");
	}

	@RequestMapping(value = "/lock", method = RequestMethod.POST)
	public ResponseEntity<CommandResponse> getLock(@ModelAttribute CommandRequest request) {
		// TODO
		CommandResponse response = new CommandResponse();
		response.setResponseType(CommandResponseType.InChannel);

		SlackAttachment releaseLockAttachment = new SlackAttachment();
		releaseLockAttachment.setText("Locked"); // TODO

		PostAction releaseLockAction = new PostAction();
		releaseLockAction.setName("Release Lock");

		PostActionIntegration releaseLockIntegration = new PostActionIntegration();
		releaseLockIntegration.setUrl(config.getBaseUrl() + "/release");

		releaseLockAction.setIntegration(releaseLockIntegration);
		releaseLockAttachment.setActions(Collections.singletonList(releaseLockAction));
		response.setAttachments(Collections.singletonList(releaseLockAttachment));

		// TODO ロックされたリソースの情報をRedisにストアする
		return ResponseEntity.ok(response);
	}

	@RequestMapping(value = "/release", method = RequestMethod.POST)
	public ResponseEntity<PostActionIntegrationResponse> releaseLock(
			@ModelAttribute PostActionIntegrationRequest request) {
		// TODO

		// TODO ロックされたリソースの情報をRedisからロードする
		// リソースがなければエラーを返す
		// リソースがあればrelease lock
		return ResponseEntity.notFound().build(); // FIXME
	}

	private void hogehoge() {
		redisTemplate.opsForValue() //
				.get(config.getRedisKey("hogehoge"));

	}
}
