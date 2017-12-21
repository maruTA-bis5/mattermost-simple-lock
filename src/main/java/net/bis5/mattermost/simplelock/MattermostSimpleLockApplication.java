package net.bis5.mattermost.simplelock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
//import net.bis5.mattermost.model.CommandRequest;
import net.bis5.mattermost.model.CommandResponse;
import net.bis5.mattermost.model.CommandResponseType;
import net.bis5.mattermost.model.Post;
import net.bis5.mattermost.model.PostAction;
import net.bis5.mattermost.model.PostAction.PostActionIntegration;
import net.bis5.mattermost.model.PostAction.PostActionIntegrationRequest;
import net.bis5.mattermost.model.PostAction.PostActionIntegrationResponse;
import net.bis5.mattermost.model.SlackAttachment;

@Component
@Path("/")
@SpringBootApplication
@Slf4j
public class MattermostSimpleLockApplication {

	public static void main(String[] args) {
		AppConfig.setupSystemProperties();

		SpringApplication.run(MattermostSimpleLockApplication.class, args);
	}

	@Autowired
	RedisTemplate<String, String> redisTemplate;
	@Autowired
	AppConfig config;

	@GET
	@Path("/ping")
	public String ping() {
		return "pong";
	}

	@POST
	@Path("/lock")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLock(@BeanParam CommandRequest request) {
		if (!config.isSameToken(request.getToken())) {
			log.warn("invalid token. expected: " + config.getToken() + ", produced: " + request.getToken());
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}

		// ロックされたリソースの情報をRedisにストアする
		String[] params = request.getText().split(" ");
		String objectName = params[0];
		String message;
		if (params.length > 1) {
			message = StringUtils.join(Arrays.asList(params).subList(1, params.length), " ");
		} else {
			message = "Locked";
		}

		String userName = request.getUserName();
		CommandResponse response = new CommandResponse();

		if (isLocked(objectName)) {
			response.setText("object [" + objectName + "] is already locked");
			response.setResponseType(CommandResponseType.Ephemeral);
			log.warn("object already locked: " + objectName);
			return Response.ok(response).build();
		}
		store(objectName);

		response.setResponseType(CommandResponseType.InChannel);
		response.setUsername(request.getUserName());

		SlackAttachment releaseLockAttachment = new SlackAttachment();
		releaseLockAttachment.setPretext(objectName + " " + message + " " + "by " + userName);

		PostAction releaseLockAction = new PostAction();
		releaseLockAction.setName("Release Lock");

		PostActionIntegration releaseLockIntegration = new PostActionIntegration();
		releaseLockIntegration.setUrl(config.getBaseUrl() + "/release");
		Map<String, String> context = new HashMap<>();
		context.put("resource", objectName);
		context.put("message", message);
		context.put("userName", userName);
		releaseLockIntegration.setContext(context);

		releaseLockAction.setIntegration(releaseLockIntegration);
		releaseLockAttachment.setActions(Collections.singletonList(releaseLockAction));
		response.setAttachments(Collections.singletonList(releaseLockAttachment));

		log.warn(response.toString());
		return Response.ok(response).build();
	}

	@Path("/release")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response releaseLock(PostActionIntegrationRequest request) {

		String name = request.getContext().get("resource");
		if (name == null) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}

		PostActionIntegrationResponse response = new PostActionIntegrationResponse();

		// リソースがなければエラーを返す
		if (!isLocked(name)) {
			log.warn("object [" + name + "] did not locked");
			response.setEphemeralText("object [" + name + "] did not locked");
			return Response.ok(response).build();
		}
		// リソースがあればrelease lock
		remove(name);

		String message = request.getContext().getOrDefault("message", "");
		String userName = request.getContext().getOrDefault("userName", "");
		String originalMessage = name + " " + message + " " + "by " + userName;
		Post update = new Post();
		update.setMessage(originalMessage + "\n" + " Lock released at " + LocalDateTime.now().toString());
		update.setProps(new HashMap<>());
		update.getProps().put("attachments", Collections.emptyList());

		response.setUpdate(update);
		return Response.ok(response).build();
	}

	private boolean isLocked(String name) {
		return redisTemplate.opsForSet().isMember(config.getRedisKey("locked"), name);
	}

	private void store(String name) {
		redisTemplate.opsForSet().add(config.getRedisKey("locked"), name);
	}

	private void remove(String name) {
		redisTemplate.opsForSet().remove(config.getRedisKey("locked"), name);
	}
}
