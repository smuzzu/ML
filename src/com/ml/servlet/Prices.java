package com.ml.servlet;

import com.ml.Preciario;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Prices extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String usuario = request.getParameter("user");
        String publication = request.getParameter("publication");
        String priceBeforeTaxesStr = request.getParameter("priceBeforeTaxes");
        String showDetailsStr = request.getParameter("mostrarMasData");
        boolean showDetails=true;
        if (showDetailsStr==null || showDetailsStr.isEmpty()){
            showDetails=false;
        }
        double priceBeforeTaxes=-1.0;
        if (priceBeforeTaxesStr!=null && !priceBeforeTaxesStr.isEmpty()){
            try {
                priceBeforeTaxes = Double.parseDouble(priceBeforeTaxesStr);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String priceStr = Preciario.getPrice2(usuario,publication, priceBeforeTaxes,showDetails);

        if (usuario==null){
            usuario="";
        }
        if (publication==null){
            publication="";
        }
        if (priceBeforeTaxesStr==null){
            priceBeforeTaxesStr="";
        }
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<link rel=\"icon\" type=\"image/x-icon\" href=\"images/sun.gif\" />");

        writer.println("<title> </title>");
        writer.println("</head>");
        writer.println("<body bgcolor=white>");
        writer.println("<table border=\"0\">");
        writer.println("<tr>");
        writer.println("<td>");
        writer.println("<img src=\"images/sun.gif\">");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("<h1>Bienvenido / Welcome</h1>");
        writer.println("<!--get back to work بھائی-->");
        writer.println("<form>");
        writer.println("Inicial de Usuario / User First Initial: <input type=\"text\" name=\"user\" value=\""+usuario+"\"/>  <br/>");
        writer.println("Numero de Publicacion / Publication Number: <input type=\"text\" name=\"publication\"value=\""+publication+"\"/>  <br/>");
        writer.println("Costo Sin Iva / Price before taxes: <input type=\"text\" name=\"priceBeforeTaxes\" value=\""+priceBeforeTaxesStr+"\"/>  <br/>");
        writer.println("<input type=\"submit\" value=\"OK\">");
        writer.println("</form>");
        writer.println(priceStr);
        writer.println("</td>");
        writer.println("</tr>");
        writer.println("</table>");
        writer.println("</body>");
        writer.println("</html>");
    }

}