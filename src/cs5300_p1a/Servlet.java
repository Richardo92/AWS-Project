package cs5300_p1a;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.concurrent.PriorityBlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/") /* relative URL path to servlet (under package name 'demoServlet'). */
public class Servlet extends HttpServlet {
	/** track the session with its session id */
	Hashtable<Integer, Session> tracker = new Hashtable<Integer, Session>();
	/** priority queue for session ordered by its expire time */
	PriorityBlockingQueue<Session> pq = new PriorityBlockingQueue<Session>(10, new Comparator<Session>() {
		@Override
		public int compare(Session o1, Session o2) {
			return o1.getExpireTime().compareTo(o2.getExpireTime());
		}
	});
	private int sessionIdBase = 0;
	/** name of cookie, constant */
	private final String COOKIENAME = "CS5300ROJ1SESSION";
	/** living time of a cookie */
	private final int LIVINGTIME = 1; // living time (ms)
	
	public Servlet() {
		/**
		 * create a thread police, run in each minute to remove expired sessions
		 */
		Thread police = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(60 * 1000); // sleep 1 minute
					while (pq.size() != 0 && pq.peek().getExpireTime().compareTo(ZonedDateTime.now()) < 0) {
						Session topSession = pq.poll();
						tracker.remove(String.valueOf(topSession.getSessionID()));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		});
		police.start();
	}
	
	/**
	 * deal with get method
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Set the MIME type for the response message
	    response.setContentType("text/html");
	    Session currSession = null;
	    Cookie[] cookies = request.getCookies();
	    if (cookies == null || cookies.length == 0) { // new client, create new cookie with new session for him
	    	currSession = createNewCookieSession(response);
	    }
	    else {
	    	/** traverse the cookie array to find the matched cookie */
	    	int i = 0;
	    	for (; i < cookies.length; i++) {
	    		if (cookies[i].getName().equals(this.COOKIENAME)) {
	    			/** find matched cookie, update the session */
	    			currSession = tracker.get(Integer.valueOf(cookies[i].getValue()));
	    			/** if this session doesn't exist or it has been expired out, create a new session */
	    			if (currSession == null || currSession.getExpireTime().compareTo(ZonedDateTime.now()) < 0) { 
	    				if (currSession != null) {
	    					tracker.remove(String.valueOf(currSession.getSessionID()));
	    					pq.remove(currSession);
	    				}
	    				currSession = createNewCookieSession(response);
	    			}
	    			else { // update the version number
	    				currSession.setVersion(currSession.getVersion() + 1);
	    				currSession.setExpireTime(LIVINGTIME);
	    				pq.remove(currSession);
	    				pq.add(currSession);
	    			}
	    			break;
	    		}
	    	}
	    	/** cannot find such cookie, create a new one */
	    	if (i >= cookies.length)
	    		currSession = createNewCookieSession(response);
	    }
	    /**
	     * print the html to client
	     */
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    /** print the basic session object */
	    if (currSession == null) { // check for unexpected situation
	    	out.println("</html>");
	    	return;
	    }
	    printSession(currSession, out);
	    printForm(out);
	    out.println("</html>");
	}
	
	/**
	 * deal with post method
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (request.getParameter("logout") != null) {
			doDelete(request, response);
			return;
		}
		response.setContentType("text/html");
	    Session currSession = null;
	    Cookie[] cookies = request.getCookies();
	    if (cookies == null || cookies.length == 0) { // new client, create new cookie with new session for him
	    	currSession = createNewCookieSession(response);
	    }
	    else {
	    	/** traverse the cookie array to find the matched cookie */
	    	int i = 0;
	    	for (; i < cookies.length; i++) {
	    		if (cookies[i].getName().equals(this.COOKIENAME)) {
	    			/** find matched cookie, update the session */
	    			currSession = tracker.get(Integer.valueOf(cookies[i].getValue()));
	    			/** if this session doesn't exist or it has been expired out, create a new session */
	    			if (currSession == null || currSession.getExpireTime().compareTo(ZonedDateTime.now()) < 0) { 
	    				if (currSession != null) {
	    					tracker.remove(String.valueOf(currSession.getSessionID()));
	    					pq.remove(currSession);
	    				}
	    				currSession = createNewCookieSession(response);
	    			}
	    			else { // update the version number
	    				currSession.setVersion(currSession.getVersion() + 1);
	    				currSession.setExpireTime(LIVINGTIME);
	    				pq.remove(currSession);
	    				pq.add(currSession);
	    				String rpText = request.getParameter("rpText");
	    				currSession.setSessionState(rpText);
	    			}
	    			break;
	    		}
	    	}
	    	/** cannot find such cookie, create a new one */
	    	if (i >= cookies.length)
	    		currSession = createNewCookieSession(response);
	    }
	    /**
	     * print the html to client
	     */
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    /** print the basic session object */
	    if (currSession == null) { // check for unexpected situation
	    	out.println("</html>");
	    	return;
	    }
	    printSession(currSession, out);
	    printForm(out);
	    out.println("</html>");
	}
	
	/**
	 * deal with delete method
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
	    Cookie[] cookies = request.getCookies();
	    if (cookies != null && cookies.length > 0) {
	    	/** traverse the cookie array to find the matched cookie */
	    	for (int i = 0; i < cookies.length; i++) {
	    		if (cookies[i].getName().equals(this.COOKIENAME)) {
	    			/** find matched cookie, delete the session */
	    			Session deleteSession = tracker.get(Integer.valueOf(cookies[i].getValue()));
	    			if (deleteSession != null) {
	    				pq.remove(deleteSession);
	    			}
	    			tracker.remove(Integer.valueOf(cookies[i].getValue()));
	    			break;
	    		}
	    	}
	    }
	    /**
	     * print the html to client
	     */
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    out.println("<p>LOG OUT SUCCESSFULLY!</p>");
	    out.println("</html>");
	}
	
	
	/**
	 * create new cookie and session for new client, respond the cookie to client
	 * return the new created session object
	 * @param response
	 */
	private Session createNewCookieSession(HttpServletResponse response) {
		int sessionId = addSessionId();
    	Cookie cookie = new Cookie(COOKIENAME, String.valueOf(sessionId));
    	response.addCookie(cookie); // add cookie into response
		Session newSession = new Session(sessionId, LIVINGTIME, 0, "Hello");
		tracker.put(sessionId, newSession); // update hash table
		pq.add(newSession);
		return newSession;
	}
	
	synchronized private int addSessionId() {
		int ret = sessionIdBase;
		sessionIdBase++; // session id++, prepare for the next new client
		return ret;
	}
	
	/**
	 * print the current session in html and respond to client
	 * @param currSession current session object
	 * @param out print writer
	 */
	private void printSession(Session currSession, PrintWriter out) {
		StringBuilder sessionSB = new StringBuilder();
		sessionSB.append("Session: ").append(currSession.getSessionID());
		sessionSB.append("&nbsp &nbsp &nbsp");
		sessionSB.append("Version: ").append(currSession.getVersion());
		sessionSB.append("&nbsp &nbsp &nbsp");
		sessionSB.append("Expire Date: ").append(currSession.getExpireTime().format(DateTimeFormatter.RFC_1123_DATE_TIME));
		out.println(sessionSB.toString());
		out.println("<h1>");
		out.println(currSession.getSessionState());
		out.println("</h1>");
	}
	
	/**
	 * print the form in html and respond to client
	 * @param out print writer
	 */
	private void printForm(PrintWriter out) {
//		<form method="post">
//			<input type="submit" name="replace" value="Replace">
//			<input type="text" name="rpText">
//		</form>
//		<form method="get">
//			<input type="submit" name="refresh" value="Refresh">
//		</form>
//		<form method="post">
//			<input type="submit" name="logout" value="Logout">
//		</form>
		out.println("<form method=\"post\">");
			out.println("<input type=\"submit\" name=\"replace\" value=\"Replace\">");
			out.println("<input type=\"text\" name=\"rpText\">");
		out.println("</form>");
		out.println("<form method=\"get\">");
			out.println("<input type=\"submit\" name=\"refresh\" value=\"Refresh\">");
		out.println("</form>");
		out.println("<form method=\"post\">");
			out.println("<input type=\"submit\" name=\"logout\" value=\"Logout\">");
		out.println("</form>");
	}
	
}
