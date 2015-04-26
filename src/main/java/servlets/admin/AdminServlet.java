package servlets.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import managers.AuthManager;
import misc.ArrayList2D;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import adt.Category;
import adt.Company;
import adt.DataVars;
import adt.ItemVars;
import adt.LayoutVars;
import adt.Response;
import adt.Response.FailResponse;
import adt.Response.SuccessResponse;

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
        // boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        // if (isMultipart) {
        //
        // Response respObj = new SuccessResponse("File Upload successful");
        //
        // // Create a new file upload handler
        // ServletFileUpload upload = new ServletFileUpload();
        //
        // try {
        // // Parse the request
        // FileItemIterator iter = upload.getItemIterator(request);
        // int i = 0;
        // while (iter.hasNext()) {
        // FileItemStream item = iter.next();
        // String name = item.getFieldName();
        // InputStream stream = item.openStream();
        // if (item.isFormField()) {
        // respObj.addToReturnData("Item " + i, "Form field " + name + " with value "
        // + Streams.asString(stream));
        // }
        // else {
        // respObj.addToReturnData("Item " + i, "File field " + name + " with file name "
        // + item.getName());
        // // Process the input stream
        // ArrayList2D arr = new ArrayList2D();
        // arr.importFromFile(new BufferedReader(new InputStreamReader(stream)), "\t", true, "\"");
        //
        // respObj.addToReturnData("Item " + i + " data", arr.toJson());
        // }
        // i++;
        // }
        // response.getWriter().print(respObj);
        // return;
        // } catch (FileUploadException fue) {
        // response.getWriter().print(new FailResponse(fue));
        // }
        // }
        
        response.setContentType("application/json");
        String method = request.getParameter("method") != null ? request.getParameter("method") : "null";
        
        Response responseObject;
        
        switch (method) {
            case "upload":
                
                boolean isMultipart = ServletFileUpload.isMultipartContent(request);
                if (isMultipart) {
                    
                    Response respObj = new SuccessResponse("File Upload successful");
                    
                    // Create a new file upload handler
                    ServletFileUpload upload = new ServletFileUpload();
                    
                    try {
                        // Parse the request
                        FileItemIterator iter = upload.getItemIterator(request);
                        int i = 0;
                        while (iter.hasNext()) {
                            FileItemStream item = iter.next();
                            String name = item.getFieldName();
                            InputStream stream = item.openStream();
                            if (item.isFormField()) {
                                respObj.addToReturnData("Item " + i, "Form field " + name + " with value "
                                        + Streams.asString(stream));
                            }
                            else {
                                respObj.addToReturnData("Item " + i, "File field " + name + " with file name "
                                        + item.getName());
                                // Process the input stream
                                ArrayList2D arr = new ArrayList2D();
                                arr.importFromFile(new BufferedReader(new InputStreamReader(stream)), "\t", true, "\"");
                                
                                respObj.addToReturnData("Item " + i + " data", arr.toJson());
                            }
                            i++;
                        }
                        response.getWriter().print(respObj);
                        responseObject = respObj;
                        return;
                    } catch (FileUploadException fue) {
                        response.getWriter().print(new FailResponse(fue));
                        responseObject = new FailResponse(fue);
                        return;
                    }
                }
                responseObject = new FailResponse("Expected content-type: multipart/form-data");
                break;
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
