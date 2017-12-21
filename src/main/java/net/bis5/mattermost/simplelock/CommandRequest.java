package net.bis5.mattermost.simplelock;

import javax.ws.rs.FormParam;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CommandRequest {

	@FormParam("token")
	private String token;
	@FormParam("team_id")
	private String teamId;
	@FormParam("team_domain")
	private String teamDomain;
	@FormParam("channel_id")
	private String channelId;
	@FormParam("channel_name")
	private String channelName;
	@FormParam("user_id")
	private String userId;
	@FormParam("user_name")
	private String userName;
	@FormParam("command")
	private String command;
	@FormParam("text")
	private String text;
	@FormParam("response_url")
	private String responseUrl;
}
