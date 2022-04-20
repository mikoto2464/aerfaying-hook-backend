package net.mikoto.aerfaying.hook.controller;

import com.alibaba.fastjson.JSONObject;
import net.mikoto.pixiv.api.pojo.Artwork;
import net.mikoto.pixiv.api.pojo.ForwardServer;
import net.mikoto.pixiv.forward.connector.ForwardConnector;
import net.mikoto.pixiv.forward.connector.exception.GetArtworkInformationException;
import net.mikoto.pixiv.forward.connector.exception.WrongSignException;
import net.mikoto.pixiv.forward.connector.factory.ForwardConnectorFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * @author mikoto
 * @date 2022/4/20 11:11
 */
@RestController
@RequestMapping(
        "/api"
)
public class HookController {
    private final ForwardConnector FORWARD_CONNECTOR;

    public HookController() {
        FORWARD_CONNECTOR = ForwardConnectorFactory.getInstance().create();
    }

    @RequestMapping(
            value = "/hook",
            method = RequestMethod.OPTIONS
    )
    public JSONObject hookRequestOptions(@NotNull HttpServletResponse httpServletResponse) {
        JSONObject outputJsonObject = new JSONObject();
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        httpServletResponse.setHeader("Access-Control-Max-Age", "2592000");
        httpServletResponse.setHeader("Timing-Allow-Origin", "*");
        httpServletResponse.setHeader("Content-Length", "0");
        httpServletResponse.setHeader("Content-Type", "application/octet-stream");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        return outputJsonObject;
    }

    @RequestMapping(
            value = "/hook",
            method = RequestMethod.POST
    )
    public JSONObject hookRequestPost(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws IOException, NoSuchMethodException, GetArtworkInformationException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, WrongSignException, IllegalAccessException {
        httpServletResponse.setHeader("Content-Type", "application/json; charset=utf-8");
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "https://aerfaying.com");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
        httpServletResponse.setHeader("Access-Control-Max-Age", "2592000");
        httpServletResponse.setHeader("Timing-Allow-Origin", "*");
        JSONObject outputJsonObject = new JSONObject();

        String data = null;

        ServletInputStream is = null;
        try {
            is = httpServletRequest.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                stringBuilder.append(new String(buf, 0, len));
            }
            data = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String targetServerAddress = null;
        int targetServerWeight = 0;
        String targetServerKey = null;
        int targetArtwork = 0;

        if (data != null) {
            String[] formData = data.split("&");

            for (String formDatum :
                    formData) {
                String[] form = formDatum.split("=");

                if ("target_server_address".equals(form[0])) {
                    targetServerAddress = URLDecoder.decode(form[1], StandardCharsets.UTF_8);
                } else if ("target_server_weight".equals(form[0])) {
                    targetServerWeight = Integer.parseInt(form[1]);
                } else if ("target_server_key".equals(form[0])) {
                    targetServerKey = form[1];
                } else if ("target_artwork".equals(form[0])) {
                    targetArtwork = Integer.parseInt(form[1]);
                }
            }
        }

        if (targetServerAddress != null && targetServerWeight != 0 && targetServerKey != null && targetArtwork != 0) {
            FORWARD_CONNECTOR.addForwardServer(new ForwardServer(targetServerAddress, targetServerWeight, targetServerKey));
            Artwork artwork = FORWARD_CONNECTOR.getArtworkInformation(targetArtwork);
            System.out.println(artwork.toJsonObject().toString());
            outputJsonObject.put("body", artwork.toJsonObject());
            outputJsonObject.put("success", true);
        } else {
            outputJsonObject.put("success", false);
        }
        return outputJsonObject;
    }
}
