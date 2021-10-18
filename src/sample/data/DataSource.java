package sample.data;

import sample.data.model.Account;
import sample.data.model.Email;
import sample.data.model.EmailConnection;
import sample.data.model.Proxy;
import sample.utils.MailConnectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DataSource {

	private LinkedList<Account> base;
	private LinkedList<Email> emails;
	private ArrayList<Proxy> proxies;
	private LinkedList<String> followers;
	private LinkedList<String> userAgents;
	private ArrayList<String> linksForLike;
	private ArrayList<String> linksForFollow;
	private ArrayList<String> bio;
	private int proxyIndex = 0;
	private final Random random = new Random();

	public Map<String, String> loadSettings() {
		Map<String, String> settings = new HashMap<>();

		File config = new File("config.ini");
		if (config.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(config))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] param = line.split("=");
					if (param.length > 1) {
						settings.put(param[0], param[1]);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return settings;
	}

	public void loadBase(File resources) {
		base = new LinkedList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(resources))) {
			String line;
			while ((line = reader.readLine()) != null) {
				Account account = new Account();

				String[] strings = line.split(";");
				if (strings.length < 2) {
					strings = line.split(":");
				}

				if (strings.length < 2) {
					continue;
				}

				String username = strings[0].trim();
				String password = strings[1].trim();
				account.setUserName(username);
				account.setPassword(password);

				if (strings.length > 2) {
					String newEmail = strings[2].trim();
					String newEmailPassword = strings[3].trim();
					account.setNewEmail(newEmail);
					account.setNewEmailPassword(newEmailPassword);
				}

				if (strings.length > 4) {
					MailConnectionUtils connectionUtils = MailConnectionUtils.getInstance();
					String domain = strings[4].split("@")[1];
					EmailConnection emailConnection = connectionUtils.findConnectionInfoByDomain(domain);
					if (emailConnection == null) {
						emailConnection = connectionUtils.createNewConnectionInfo(domain);
					}

					String email = strings[4].trim();
					String emailPassword = strings[5].trim();
					account.setOldEmail(email);
					account.setOldEmailPassword(emailPassword);
					account.setEmailConnection(emailConnection);
				}

				base.add(account);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadEmails(File resources) {
		emails = new LinkedList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(resources))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] strings = line.split(";");
				if (strings.length < 2) {
					strings = line.split(":");
				}
				if (strings.length >= 2 && strings[0].contains("@")) {
					MailConnectionUtils connectionUtils = MailConnectionUtils.getInstance();
					String domain = strings[0].split("@")[1];
					EmailConnection emailConnection = connectionUtils.findConnectionInfoByDomain(domain);
					if (emailConnection == null) {
						emailConnection = connectionUtils.createNewConnectionInfo(domain);
						System.out.println(emailConnection.toString());
					}
					String email = strings[0].trim();
					String password = strings[1].trim();
					if (password.contains(" ")) {
						password = password.substring(0, password.indexOf(' '));
					}
					emails.add(new Email(email, password, emailConnection));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadProxies(File resources, Proxy.Type type) {
		proxies = new ArrayList<>();
		proxyIndex = 0;

		try (BufferedReader reader = new BufferedReader(new FileReader(resources))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] strings = line.split(":");
				if (!strings[0].contains(".")) {
					continue;
				}

				if (strings.length > 2) {
					proxies.add(new Proxy(strings[0], strings[1], strings[2], strings[3], type));
				} else {
					proxies.add(new Proxy(strings[0], strings[1], "", "", type));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadFollowers(File resources) {
		followers = new LinkedList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(resources))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] strings = line.split(":");
				if (!line.isEmpty()) {
					followers.add(strings[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadUserAgents(File resources) {
		userAgents = new LinkedList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(resources))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty()) {
					userAgents.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized Account getBaseLine() {
		return base.pollFirst();
	}

	public synchronized Email getEmail() {
		return emails.pollFirst();
	}

	public synchronized Proxy getProxy(int thread) {
		if (proxies == null || proxies.isEmpty() || thread > proxies.size()) {
			return null;
		}

		return proxies.get(thread);
	}

	public synchronized Proxy getProxy() {
		if (proxies == null || proxies.isEmpty()) {
			return null;
		}

		Proxy proxy;
		do {
			proxy = proxies.get(proxyIndex);
			if (proxyIndex++ >= proxies.size() - 1) {
				proxyIndex = 0;
			}
		}
		while (!proxy.isValid());

		return proxy;
	}

	public synchronized void insertProxy(Proxy proxy) {
		int index = proxies.indexOf(proxy);
		proxies.remove(proxy);
		proxies.add(proxyIndex, proxy);
		if (index < proxyIndex) {
			proxyIndex--;
		}
	}

	public String getFollowers(int index) {
		return followers.get(index);
	}

	public synchronized String getUserAgent() {
		if (userAgents == null || userAgents.isEmpty()) {
			return null;
		}

		String userAgent;
		do {
			int index = random.nextInt(userAgents.size());
			userAgent = userAgents.get(index);
		} while (!userAgent.contains("Mobile"));

		return userAgent;
	}

	public int getProxiesListSize() {
		return proxies != null ? proxies.size() : 0;
	}

	public int getBaseSize() {
		return base != null ? base.size() : 0;
	}

	public int getEmailsSize() {
		return emails != null ? emails.size() : 0;
	}

	public int getFollowersSize() {
		return followers != null ? followers.size() : 0;
	}

	public LinkedList<String> getFollowersList() {
		return new LinkedList<>(followers);
	}

	public int getUserAgentsSize() {
		return userAgents != null ? userAgents.size() : 0;
	}

	public void loadLinksForLike(File resources) {
		linksForLike = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(resources))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty()) {
					linksForLike.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized ArrayList<String> getLinksForLike() {
		return linksForLike;
	}

	public int getLinksForLikeSize() {
		return linksForLike != null ? linksForLike.size() : 0;
	}

	public void loadLinksForFollow(File resource) {
		linksForFollow = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(resource))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty()) {
					linksForFollow.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getLinksForFollow() {
		return linksForFollow;
	}

	public int getLinksForFollowSize() {
		return linksForFollow != null ? linksForFollow.size() : 0;
	}

	public void loadBio(File resources) {
		bio = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resources),
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty()) {
					bio.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized String getBio() {
		return bio.get(random.nextInt(bio.size() - 1));
	}

	public int getBioSize() {
		return bio != null ? bio.size() : 0;
	}
}
