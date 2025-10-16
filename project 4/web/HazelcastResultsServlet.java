import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

@WebServlet("/results")
public class HazelcastResultsServlet extends HttpServlet {

    private HazelcastInstance hz;

    @Override
    public void init() throws ServletException {
        hz = Hazelcast.newHazelcastInstance();  // use config if needed
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<h1>Aggregated Results from All Children</h1>");
        IMap<String, Map<String, Integer>> resultMap = hz.getMap("results");

        for (String agentId : resultMap.keySet()) {
            out.println("<h3>Results from: " + agentId + "</h3><ul>");
            Map<String, Integer> fileMap = resultMap.get(agentId);
            for (Map.Entry<String, Integer> entry : fileMap.entrySet()) {
                out.println("<li>File[" + entry.getKey() + "] has " + entry.getValue() + "</li>");
            }
            out.println("</ul>");
        }
    }

    @Override
    public void destroy() {
        hz.shutdown();
    }
}
