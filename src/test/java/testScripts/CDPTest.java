package testScripts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v117.network.Network;
import org.openqa.selenium.devtools.v117.network.model.Headers;
import org.openqa.selenium.devtools.v117.network.model.RequestWillBeSent;
import org.testng.Assert;
import org.testng.annotations.*;



public class CDPTest {
	ChromiumDriver driver;
	DevTools devTools;
	
	@BeforeMethod
	public void setup() {
		driver = new ChromeDriver();
		devTools = driver.getDevTools();
		devTools.createSession();
	}
	
//	@Test
	public void deviceModeTest() {
		Map deviceMetrics = new HashMap() {{
			put("width", 800);
			put("height", 1000);
			put("deviceScaleFactor", 50);
			put("mobile", true);
			
		}};
		driver.executeCdpCommand("Emulation.setDeviceMetricsOverride", deviceMetrics);
		driver.get("https://www.selenium.dev/");
	}
	
//	@Test
	public void geoLocationTest() {
		Map deviceMetrics = new HashMap() {{
			put("latitude", 41.878113);
			put("longitude", -87.629799);
			put("accuracy", 100);
		}};
		driver.executeCdpCommand("Emulation.setGeolocationOverride", deviceMetrics);
		driver.get("https://oldnavy.gap.com/stores");
	}
	
//	@Test
	public void captureNWTrafficTest() {
//		Map deviceMetrics = new HashMap() {
//			{
//			put("maxTotalBufferSize", 1000);
//			put("maxResourceBufferSize", 1000);
//			put("maxPostDataSize", 1000);
//			}
//		};
		
//		driver.executeCdpCommand("Network.enable", deviceMetrics);
		
		devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		devTools.addListener(Network.requestWillBeSent(), new Consumer<RequestWillBeSent>() {
			public void accept(RequestWillBeSent entry) {
				System.out.println("Request URI : " + entry.getRequest().getUrl() + "\n" 
						+ " With method : "
						+ entry.getRequest().getMethod() + "\n");
				entry.getRequest().getMethod();
			}
		});
//		driver.executeCdpCommand("Emulation.setGeolocationOverride", deviceMetrics);
		driver.get("https://www.selenium.dev/");
		devTools.send(Network.disable());
	}
	
	@Test
	public void basicAuthTest() {
		devTools.send(Network.enable(Optional.empty(),Optional.empty(), Optional.empty()));
		Map<String, Object> headers = new HashMap();
		String strUser = "admin";
		String strPwd = "admin";
		
		String basicAuth = "Basic "
				+ new String(new Base64().encode(String.format("%s:%s", strUser, strPwd)
						.getBytes()));
		
		System.out.println("Auth...  " + basicAuth);
		//Set Header - Auth Token
		headers.put("Authorization", basicAuth);
		devTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));
		
		driver.get("https://the-internet.herokuapp.com/basic_auth");
		String strMsg = driver.findElement(By.cssSelector("div.example p")).getText();
		Assert.assertEquals(strMsg, "Congratulations! You must have the proper credentials.");
	}
	
	@AfterMethod
	public void teardown() throws InterruptedException {
		Thread.sleep(5000);
		driver.close();
	}

}
