package servlets.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import managers.AuthManager;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import adt.Category;
import adt.Company;
import adt.DataVars;
import adt.ItemVars;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;

@WebServlet("/api/users/admin")
@MultipartConfig(location = "/var/lib/openshift/5514734a4382ec499b000009/app-root/data")
public class AdminServlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long                serialVersionUID = -5982008108929904358L;
    
    public static DataVars                   dataVars;
    public static LayoutVars                 layoutVars;
    public static ItemVars                   systemVars;
    public static HashMap<Integer, Category> categoryMap      = new HashMap<Integer, Category>();
    public static HashMap<Integer, Company>  entryMap         = new HashMap<Integer, Company>();
    
    public static boolean                    isTest           = true;
    
    /** Getter & Setter Methods **/
    
    public AdminServlet() throws IOException {
    
        super();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        Response authResponse;
        if (!(authResponse = AuthManager.checkToken(request)).success) {
            
            response.getWriter().print(authResponse);
            return;
        }
        
        response.setContentType("application/json");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            default:
                responseObject = new FailResponse("Invalid GET method supplied: " + method);
                break;
        }
        response.getWriter().print(responseObject);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
        Response authResponse;
        if (!(authResponse = AuthManager.checkToken(request)).success) {
            
            response.getWriter().print(authResponse);
            return;
        }
        
        PrintWriter out = response.getWriter();
        
        out.print("Request content length is " + request.getContentLength() + "<br/>");
        out.print("Request content type is " + request.getHeader("Content-Type") + "<br/>");
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {
                // Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                
                // Configure a repository (to ensure a secure temp location is used)
                ServletContext servletContext = this.getServletConfig().getServletContext();
                File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                factory.setRepository(repository);
                
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                
                // Parse the request
                List<FileItem> items = upload.parseRequest(request);
                
            } catch (FileUploadException fue) {
                out.print(new FailResponse(fue));
            }
        }
        
        response.setContentType("application/json");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            case "newTerm":
                responseObject = AdminRequestHandler.handleNewTermRequest(request);
                break;
            case "setSize":
                responseObject = AdminRequestHandler.handleSetSizeRequest(request);
                break;
            default:
                responseObject = new FailResponse("Invalid POST method supplied: " + method);
                break;
        }
        response.getWriter().print(responseObject);
    }
}
