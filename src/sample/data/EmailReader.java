package sample.data;


import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IdleManager;
import com.sun.mail.util.MailConnectException;
import org.apache.commons.lang3.StringUtils;
import sample.data.model.Account;
import sample.data.model.Email;
import sample.data.model.EmailResult;
import sample.data.model.Proxy;
import sample.utils.Logger;
import sample.utils.MessageReceivedObservable;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static sample.utils.Constants.TIMER_PERIOD;

public class EmailReader {

	public enum Type {
		LINK, CODE
	}

	private static final String INBOX_FOLDER = "INBOX";
	private static final List<String> INVALID_CREDENTIALS = new ArrayList<>();
	private static final List<String> CONNECTIONS_ERRORS = new ArrayList<>();

	private static final String INVALID_HOSTNAME = "Couldn't connect to host";
	private static final String CODE_VERIFICATION_SEARCH_PATTERN_BEGIN = ":</p><p style=\"margin:10px 0 10px 0;color:#565a5c;font-size:18px;\"><font size=\"6\">";
	private static final String CODE_VERIFICATION_SEARCH_PATTERN_END = "</font></p><p style=\"margin:10px 0 10px 0;color:#565a5c;font-size:18px;\">";
	private static final String RESET_PASSWORD_TITLE = "Reset Your Password";
	private static final String CONFIRM_EMAIL_TITLE = "Confirm your email address for Instagram";
	private static final String SECURE_ACCOUNT_EMAIL_TITLE = "Your Instagram password has been changed";
	private static final String SECURITY_EMAIL_FROM = "security@mail.instagram.com";
	private static final String NO_REPLY_EMAIL_FROM = "no-reply@mail.instagram.com";
	private static final String CONFIRM_LINK_SUBSTRING = "https://instagram.com/accounts/confirm_email/";
	private static final String IDLE_NOT_SUPPORTED = "IDLE not supported";
	private static final String IDLE_ERROR = "Folder is not using SocketChannels";

	private final Logger logger;
	private IdleManager idleManager;
	private Timer timer;
	private final MessageReceivedObservable codeVerificationMessageReceived = new MessageReceivedObservable();

	public EmailReader(Logger logger) {
		this.logger = logger;
	}

	public MessageReceivedObservable getCodeVerificationMessageReceived() {
		return codeVerificationMessageReceived;
	}

	static {
		INVALID_CREDENTIALS.add("Invalid login or password".toUpperCase());
		INVALID_CREDENTIALS.add("Invalid user name or password".toUpperCase());
		INVALID_CREDENTIALS.add("ERROR 119 invalid user or password err 30".toUpperCase());
		INVALID_CREDENTIALS.add("Invalid login or password -".toUpperCase());
		INVALID_CREDENTIALS.add("AUTHENTICATE failed: Invalid login".toUpperCase());
		INVALID_CREDENTIALS.add("[AUTHENTICATIONFAILED] Authentication failed.".toUpperCase());
		INVALID_CREDENTIALS.add("Auth-Status: Invalid login or password".toUpperCase());
		INVALID_CREDENTIALS.add("AUTHENTICATE failed.".toUpperCase());
		INVALID_CREDENTIALS.add("Invalid login".toUpperCase());
		INVALID_CREDENTIALS.add("[AUTHENTICATIONFAILED] Bad username or password".toUpperCase());
		INVALID_CREDENTIALS.add("authentication failed".toUpperCase());
		INVALID_CREDENTIALS.add("Failed.".toUpperCase());
		INVALID_CREDENTIALS.add("Login failed.".toUpperCase());
		INVALID_CREDENTIALS.add("[AUTHENTICATIONFAILED] AUTHENTICATE Invalid credentials".toUpperCase());
		INVALID_CREDENTIALS.add("[AUTHENTICATIONFAILED] Invalid credentials (Failure)".toUpperCase());

		CONNECTIONS_ERRORS.add("Connection dropped by server?".toUpperCase());
		CONNECTIONS_ERRORS.add("connection failure".toUpperCase());
		CONNECTIONS_ERRORS.add("BYE disconnecting".toUpperCase());
		CONNECTIONS_ERRORS.add("Couldn't connect to host".toUpperCase());
		CONNECTIONS_ERRORS.add("Authentication failed".toUpperCase());
	}

	public EmailResult setupListener(Email email, Proxy proxy) {
		codeVerificationMessageReceived.setReceived(false);

		String protocol = email.getEmailConnection().getType();
		EmailResult emailResult = new EmailResult();
		ExecutorService es = Executors.newCachedThreadPool();
		Properties properties = getProperties(email, proxy, protocol);
		properties.put("mail.event.scope", "session");
		properties.replace("mail.event.executor", es);
		properties.put("mail." + protocol + ".usesocketchannels", "true");

		Session session = Session.getDefaultInstance(properties);
		session.setDebug(true);
		try {
			Store store = session.getStore(protocol);
			store.connect(email.getEmailConnection().getHostname(), email.getEmailConnection().getPort(), email.getEmail(), email.getPassword());

			idleManager = new IdleManager(session, es);
			MessageCountAdapter listener = new MessageCountAdapter() {
				public void messagesAdded(MessageCountEvent ev) {
					Folder folder = (Folder) ev.getSource();
					if (!folder.isOpen()) {
						try {
							store.connect(email.getEmailConnection().getHostname(), email.getEmailConnection().getPort(),
									email.getEmail(), email.getPassword());
						} catch (MessagingException e) {
							e.printStackTrace();
						}
					}

					Message[] messages = ev.getMessages();
					System.out.println("EMail: " + email.getEmail() + ". Folder: " + folder + " got " + messages.length + " new messages");
					for (Message message : messages) {
						parseNewMessage(message);
					}

					try {
						if (folder.isOpen()) {
							idleManager.watch(folder);
						}
					} catch (MessagingException ex) {
						logger.printError(ex);
						ex.printStackTrace();
					}
				}
			};

			List<Folder> folders = Arrays.stream(store.getDefaultFolder().list("*"))
					.filter(folder -> {
						try {
							IMAPFolder imapFolder = (IMAPFolder) folder;
							for (String attr : imapFolder.getAttributes()) {
								if (attr.equals("\\Junk")) return true;
							}
						} catch (MessagingException | ClassCastException e) {
							e.printStackTrace();
						}
						return false;
					})
					.collect(Collectors.toList());

			try {
				Folder folder = store.getFolder(INBOX_FOLDER);
				folder.open(Folder.READ_WRITE);
				folder.addMessageCountListener(listener);
				idleManager.watch(folder);

				createCustomEmailListener(folders);
			} catch (MessagingException e) {
				if (e.getMessage().equalsIgnoreCase(IDLE_NOT_SUPPORTED) || e.getMessage().equalsIgnoreCase(IDLE_ERROR)) {
					System.out.println(IDLE_NOT_SUPPORTED);
					folders.add(store.getFolder(INBOX_FOLDER));
					createCustomEmailListener(folders);
				}
			}
		} catch (MessagingException | IOException e) {
			exceptionHandling(emailResult, e);
			return emailResult;
		}
		emailResult.setSuccessfully(true);

		return emailResult;
	}

	private void createCustomEmailListener(List<Folder> folders) throws MessagingException {
		Map<String, Integer> messagesInFolders = new HashMap<>();
		for (Folder folder : folders) {
			if (!folder.isOpen()) {
				folder.open(Folder.READ_ONLY);
			}
			messagesInFolders.put(folder.getName(), folder.getMessageCount());
		}

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				for (Folder folder : folders) {
					try {
						int newMessages = folder.getMessageCount() - messagesInFolders.get(folder.getName());
						if (newMessages > 0) {
							System.out.println("Folder: " + folder + " got " + newMessages + " new messages");
							int count = folder.getMessageCount();
							Message[] messages = folder.getMessages(count - newMessages, count);
							for (Message message : messages) {
								parseNewMessage(message);
							}
							messagesInFolders.replace(folder.getName(), count);
						}
					} catch (MessagingException e) {
						if (e instanceof FolderClosedException) {
							if (!folder.isOpen()) {
								try {
									folder.open(Folder.READ_ONLY);
								} catch (MessagingException ex) {
									ex.printStackTrace();
								}
							}
						}
						e.printStackTrace();
					}
				}
			}
		}, TIMER_PERIOD, TIMER_PERIOD);
	}

	private void parseNewMessage(Message message) {
		try {
			String subject = message.getSubject() != null ? message.getSubject() : "";
			String address = ((InternetAddress) message.getFrom()[0]).getAddress();
			String content = message.getContent().toString();

			if (subject.equalsIgnoreCase(SECURE_ACCOUNT_EMAIL_TITLE) && address.equalsIgnoreCase(NO_REPLY_EMAIL_FROM)) {
				String link = StringUtils.substringBetween(content, "<a href=\"", "\"");
				System.out.println(link);
//				passwordChangedMessageReceived.setResult(link);
			} else if (subject.equalsIgnoreCase(RESET_PASSWORD_TITLE) && address.equalsIgnoreCase(SECURITY_EMAIL_FROM)) {
				String link = StringUtils.substringBetween(content, "<a href=\"", "\"");
				System.out.println(link);
//				resetPasswordMessageObservable.setResult(link);
			} else if (address.equalsIgnoreCase(SECURITY_EMAIL_FROM)) {
				if (content.contains(CODE_VERIFICATION_SEARCH_PATTERN_BEGIN) && content.contains(CODE_VERIFICATION_SEARCH_PATTERN_END)) {
					String code = StringUtils.substringBetween(content, "<font size=\"6\">", "</font></p>");
					if (code != null && !code.isEmpty()) {
						System.out.println(code);
						codeVerificationMessageReceived.setResult(code);
						message.setFlag(Flags.Flag.DELETED, true);
					}
				}
			}
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
			logger.printError(e);
		}
	}

	public void stopNewMailsObserving() {
		if (idleManager != null && idleManager.isRunning()) {
			idleManager.stop();
		}

		if (timer != null) {
			timer.cancel();
		}
	}

	public EmailResult connectToEmail(Account account, Proxy proxy) {
		EmailResult emailResult = new EmailResult();
		Properties properties = getProperties(account, proxy, account.getEmailConnection().getType());

		Session session = Session.getDefaultInstance(properties);
		session.setDebug(true);
		Store store = null;
		try {
			store = session.getStore(account.getEmailConnection().getType());
			store.connect(account.getEmailConnection().getHostname(), account.getEmailConnection().getPort(), account.getOldEmail(), account.getOldEmailPassword());

			store.getFolder(INBOX_FOLDER);
			emailResult.setSuccessfully(true);
		} catch (MessagingException e) {
			if (e instanceof MailConnectException && e.getMessage().startsWith(INVALID_HOSTNAME)) {
				account.getEmailConnection().useReservedHostname();
				logger.info("Невалідний хост. Переключення на резервний");
			}
			exceptionHandling(emailResult, e);
		} finally {
			closeStore(store);
		}

		return emailResult;
	}

	public EmailResult getVerificationCode(Email email, Proxy proxy) {
		logger.info("Пошук листа з кодом");

		String protocol = email.getEmailConnection().getType();
		String code = "";
		EmailResult emailResult = new EmailResult();
		Properties properties = getProperties(email, proxy, protocol);

		Session session = Session.getDefaultInstance(properties);
		Store store = null;
		try {
			store = session.getStore(protocol);
			store.connect(email.getEmailConnection().getHostname(), email.getEmailConnection().getPort(), email.getEmail(), email.getPassword());

			int messagesForReview = protocol.equalsIgnoreCase("pop3") ? 20 : 50;
			code = searchInFolder(store.getFolder(INBOX_FOLDER), "", SECURITY_EMAIL_FROM, Type.CODE, messagesForReview);
			if (code.isEmpty()) {
				code = checkAnotherFolders(store, "", SECURITY_EMAIL_FROM, Type.CODE, protocol);
			}
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
		} finally {
			closeStore(store);
		}

		if (!code.isEmpty()) {
			emailResult.setResult(code);
			emailResult.setSuccessfully(true);
		} else {
			emailResult.setSuccessfully(false);
		}

		return emailResult;
	}

	public EmailResult getConfirmEmailLink(Email email, Proxy proxy) {
		logger.info("Пошук листа для підтвердження пошти " + email.getEmail());

		String protocol = email.getEmailConnection().getType();
		String confirmEmailLink = "";
		EmailResult emailResult = new EmailResult();
		Properties properties = getProperties(email, proxy, protocol);

		Session session = Session.getDefaultInstance(properties);
		session.setDebug(true);
		Store store = null;
		try {
			store = session.getStore(protocol);
			store.connect(email.getEmailConnection().getHostname(), email.getEmailConnection().getPort(), email.getEmail(), email.getPassword());

			int messagesForReview = protocol.equalsIgnoreCase("pop3") ? 20 : 50;
			confirmEmailLink = searchInFolder(store.getFolder(INBOX_FOLDER), CONFIRM_EMAIL_TITLE, NO_REPLY_EMAIL_FROM, Type.LINK, messagesForReview);
			if (confirmEmailLink.isEmpty()) {
				confirmEmailLink = checkAnotherFolders(store, CONFIRM_EMAIL_TITLE, NO_REPLY_EMAIL_FROM, Type.LINK, protocol);
			}
		} catch (MessagingException | IOException e) {
			exceptionHandling(emailResult, e);
		} finally {
			closeStore(store);
		}

		if (confirmEmailLink.startsWith(CONFIRM_LINK_SUBSTRING)) {
			emailResult.setResult(confirmEmailLink);
			emailResult.setSuccessfully(true);
		} else {
			emailResult.setSuccessfully(false);
		}

		return emailResult;
	}

	private String checkAnotherFolders(Store store, String subject, String address, Type type, String protocol) throws MessagingException, IOException {
		List<Folder> folders = Arrays.stream(store.getDefaultFolder().list("*"))
				.collect(Collectors.toList());

		System.out.println(folders);

		int messagesForReview = protocol.equalsIgnoreCase("pop3") ? 20 : 40;
		for (Folder folder : folders) {
			String link = searchInFolder(folder, subject, address, type, messagesForReview);
			if (link != null && !link.isEmpty()) {
				return link;
			}
		}

		return "";
	}

	private String searchInFolder(Folder folder, String subject, String address, Type type, int messagesForReview) throws MessagingException, IOException {
		System.out.println(folder.getName());
		String result = "";
		try {
			folder.open(Folder.READ_ONLY);
			int count = folder.getMessageCount();
			if (count == 0) {
				return result;
			}

			Message[] messages = folder.getMessages(count > messagesForReview ? count - messagesForReview : 1, count);
			for (int i = messages.length - 1; i >= 0; i--) {
				String subj = messages[i].getSubject();
				if (subj == null || messages[i].getFrom() == null || messages[i].getFrom().length == 0) {
					continue;
				}
				String addr = ((InternetAddress) messages[i].getFrom()[0]).getAddress();

				if (type == Type.LINK) {
					if (subj.equalsIgnoreCase(subject) && addr.equalsIgnoreCase(address)) {
						result = StringUtils.substringBetween(messages[i].getContent().toString(), "<a href=\"", "\"");
						System.out.println(result);
						break;
					}
				}  //					String content = messages[i].getContent().toString();
				//					if (addr.equalsIgnoreCase(address) && content.contains(CODE_VERIFICATION_SEARCH_PATTERN_BEGIN)
				//							&& content.contains(CODE_VERIFICATION_SEARCH_PATTERN_END)) {
				//						result = StringUtils.substringBetween(content, "<font size=\"6\">", "</font></p>");
				//						if (result != null && !result.isEmpty()) {
				//							System.out.println(result);
				//							break;
				//						}
				//					}

			}
		} finally {
			if (folder.isOpen()) {
				folder.close(false);
			}
		}

		return result;
	}

	private Properties getProperties(Account account, Proxy proxy, String protocol) {
		Properties properties = new Properties();
		properties.put("mail." + protocol + ".connectiontimeout", 30000);
		properties.put("mail." + protocol + ".timeout", 30000);

		if (account.getEmailConnection().getPort() == 993) {
			properties.put("mail.imaps.ssl.trust", "*");
		}

		if (proxy != null) {
			setupProxy(proxy, properties, protocol);
		}

		return properties;
	}

	private Properties getProperties(Email email, Proxy proxy, String protocol) {
		Properties properties = new Properties();
		properties.put("mail." + protocol + ".connectiontimeout", 30000);
		properties.put("mail." + protocol + ".timeout", 30000);

		if (email.getEmailConnection().getPort() == 993) {
			properties.put("mail.imaps.ssl.trust", "*");
		}

		if (proxy != null) {
			setupProxy(proxy, properties, protocol);
		}

		return properties;
	}

	private void closeStore(Store store) {
		if (store != null) {
			try {
				store.close();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	private void exceptionHandling(EmailResult emailResult, Exception e) {
		emailResult.setException(e);

		String msg = e.getMessage();
		if (e instanceof AuthenticationFailedException) {
			emailResult.setInvalidCredentials(true);
		} else if (e instanceof SocketTimeoutException) {
			emailResult.setNeedReconnect(true);
			logger.printError(e);
			e.printStackTrace();
		} else {
			logger.info(msg);
			if (msg == null) return;
			for (String error : CONNECTIONS_ERRORS) {
				if (msg.toUpperCase().contains(error)) {
					emailResult.setNeedReconnect(true);
					e.printStackTrace();
				}
			}
		}
	}

	private void setupProxy(Proxy proxy, Properties properties, String protocol) {
		properties.put("mail.socket.debug", "true");
		properties.put("proxySet", "true");
		switch (proxy.getType()) {
			case HTTP:
				properties.replace("mail." + protocol + ".proxy.host", proxy.getHost());
				properties.replace("mail." + protocol + ".proxy.port", proxy.getPort());
				if (proxy.isAuthorized()) {
					properties.replace("mail." + protocol + ".proxy.user", proxy.getLogin());
					properties.replace("mail." + protocol + ".proxy.password", proxy.getPassword());
				}
				break;

			case SOCKS:
				properties.replace("mail." + protocol + ".socks.host", proxy.getHost());
				properties.replace("mail." + protocol + ".socks.port", proxy.getPort());
				//todo Add authorization
		}
	}
}