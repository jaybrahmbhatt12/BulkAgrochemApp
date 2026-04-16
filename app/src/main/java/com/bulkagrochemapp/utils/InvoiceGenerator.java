package com.bulkagrochemapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Order;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceGenerator {

    public static void generateInvoice(Context context, Order order) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        TextPaint textPaint = new TextPaint();

        // Page size A4 (approx 595 x 842 points)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int x = 40;
        int y = 50;

        // Company Logo set in center
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_bulkagro);
        if (logo != null) {
            int logoWidth = 120;
            int logoHeight = 40;
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoWidth, logoHeight, false);
            int centerX = (595 - logoWidth) / 2;
            canvas.drawBitmap(scaledLogo, centerX, y - 25, null);
            y += 30; // Move y down after logo
        }

        // Brand Name removed as requested
        
        x = 40; // Reset x
        y += 40;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(16);
        paint.setColor(Color.BLACK);
        canvas.drawText("Order Details", x, y, paint);

        y += 25;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(12);
        
        canvas.drawText("Invoice Number : " + (order.getOrderNumber() != null ? order.getOrderNumber() : String.format(Locale.getDefault(), "ORD%08d", order.getId())), x, y, paint);
        y += 15;
        
        String orderDate = order.getCreatedAt();
        if (orderDate != null) {
            orderDate = orderDate.replace("T", " ");
            if (orderDate.contains(".")) {
                orderDate = orderDate.substring(0, orderDate.indexOf("."));
            } else if (orderDate.contains("Z")) {
                orderDate = orderDate.replace("Z", "");
            }
        }
        canvas.drawText("Order Date : " + (orderDate != null ? orderDate : "N/A"), x, y, paint);
        y += 15;
        
        canvas.drawText("Order ID : " + order.getId(), x, y, paint);
        y += 15;
        
        String shippingMethod = order.getShipping();
        if ("shipto".equalsIgnoreCase(shippingMethod)) {
            shippingMethod = "Ship To Address";
        }
        canvas.drawText("Shipping Method : " + (shippingMethod != null ? shippingMethod : "N/A"), x, y, paint);
        y += 15;
        
        canvas.drawText("Payment Method : " + (order.getMethod() != null ? order.getMethod() : "N/A"), x, y, paint);
        y += 15;

        canvas.drawText("Payment Status : " + (order.getPaymentStatus() != null ? order.getPaymentStatus() : "N/A"), x, y, paint);
        y += 15;

        canvas.drawText("Delivery Status : " + (order.getStatus() != null ? order.getStatus() : "N/A"), x, y, paint);

        y += 40;
        int billingX = 300;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Shipping Details", x, y, paint);
        canvas.drawText("Billing Details", billingX, y, paint);

        y += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textPaint.set(paint);
        int addressWidth = 220; // Max width for the address to wrap

        // Fetch state logic
        String state = order.getCustomerState();
        if (state == null || state.trim().isEmpty() || state.equalsIgnoreCase("null")) {
            // Try to extract from address if blank
            String fullAddr = order.getCustomerAddress();
            if (fullAddr != null && fullAddr.contains(",")) {
                String[] parts = fullAddr.split(",");
                if (parts.length >= 2) {
                    state = parts[parts.length - 1].trim();
                    // Basic check to see if it's likely a state (not just a zip)
                    if (state.matches(".*\\d{6}.*")) {
                        // If it has a zip, try previous part
                        if (parts.length >= 3) {
                             state = parts[parts.length - 2].trim();
                        }
                    }
                }
            }
        }
        if (state == null || state.equalsIgnoreCase("null")) state = "";

        // Shipping Details
        String shippingName = order.getShippingName();
        if (shippingName == null || shippingName.isEmpty() || shippingName.equalsIgnoreCase("null")) {
            shippingName = order.getCustomerName();
        }
        canvas.drawText("Customer Name: " + shippingName, x, y, paint);
        int shippingY = y + 15;
        canvas.drawText("Phone: " + order.getCustomerPhone(), x, shippingY, paint);
        shippingY += 15;
        List<String> shippingAddressLines = wrapText("Address: " + order.getCustomerAddress(), addressWidth, textPaint);
        for (String line : shippingAddressLines) {
            canvas.drawText(line, x, shippingY, textPaint);
            shippingY += 15;
        }
        canvas.drawText("City: " + order.getCustomerCity(), x, shippingY, paint);
        shippingY += 15;
        canvas.drawText("State: " + state, x, shippingY, paint); 
        shippingY += 15;
        canvas.drawText("Country: India", x, shippingY, paint);

        // Billing Details
        canvas.drawText("Customer Name: " + order.getCustomerName(), billingX, y, paint);
        int billingY = y + 15;
        canvas.drawText("Phone: " + order.getCustomerPhone(), billingX, billingY, paint);
        billingY += 15;
        List<String> billingAddressLines = wrapText("Address: " + order.getCustomerAddress(), addressWidth, textPaint);
        for (String line : billingAddressLines) {
            canvas.drawText(line, billingX, billingY, textPaint);
            billingY += 15;
        }
        canvas.drawText("City: " + order.getCustomerCity(), billingX, billingY, paint);
        billingY += 15;
        canvas.drawText("State: " + state, billingX, billingY, paint);
        billingY += 15;
        canvas.drawText("Country: India", billingX, billingY, paint);
        
        y = Math.max(shippingY, billingY) + 30;

        // Table Header
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawRect(x, y, 555, y + 25, getHeaderPaint());
        paint.setColor(Color.WHITE);
        canvas.drawText("Product", x + 10, y + 17, paint);
        canvas.drawText("Details", x + 250, y + 17, paint);
        canvas.drawText("Total", x + 450, y + 17, paint);

        y += 25;
        paint.setColor(Color.BLACK);
        
        double calculatedSubtotal = 0;
        textPaint.setTextSize(12);
        textPaint.setColor(Color.BLACK);

        try {
            if (order.getCart() != null) {
                JSONArray cartArray = new JSONArray(order.getCart());
                for (int i = 0; i < cartArray.length(); i++) {
                    JSONObject cartObj = cartArray.getJSONObject(i);
                    String name = cartObj.optString("name");
                    double price = cartObj.optDouble("price", 0);
                    if (price == 0) price = cartObj.optDouble("prices", 0);
                    if (price == 0) price = cartObj.optDouble("size_price", 0);

                    int qty = cartObj.optInt("qty", 0);
                    if (qty == 0) qty = cartObj.optInt("quantity", 0);

                    double itemTaxTotal = cartObj.optDouble("tax", 0); 
                    
                    // Fetch tax percentage from JSON keys (taxpro, tax_rate, or taxRate)
                    double taxPercentage = 0;
                    if (cartObj.has("taxpro")) {
                        taxPercentage = cartObj.optDouble("taxpro");
                    } else if (cartObj.has("tax_rate")) {
                        taxPercentage = cartObj.optDouble("tax_rate");
                    } else if (cartObj.has("taxRate")) {
                        taxPercentage = cartObj.optDouble("taxRate");
                    }
                    
                    double itemTotal = price * qty;
                    calculatedSubtotal += itemTotal;

                    y += 20;
                    
                    // Handle product name wrapping
                    List<String> wrappedName = wrapText(name, 200, textPaint);
                    int nameStartY = y;
                    for (int line = 0; line < wrappedName.size(); line++) {
                        if (y > 780) {
                            pdfDocument.finishPage(page);
                            page = pdfDocument.startPage(pageInfo);
                            canvas = page.getCanvas();
                            y = 50;
                            nameStartY = y;
                        }
                        canvas.drawText(wrappedName.get(line), x + 10, y, textPaint);
                        if (line < wrappedName.size() - 1) y += 15;
                    }
                    
//                    // Draw Tax Percentage below product name
//                    y += 15;
//                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
//                    paint.setTextSize(10);
//                    canvas.drawText("Tax: " + String.format(Locale.getDefault(), "%.1f", taxPercentage) + "%", x + 10, y, paint);
//                    paint.setTextSize(12); // Reset size

                    // Details section (Price, Qty) - Align with start of name
                    int detailsY = nameStartY;
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    canvas.drawText("Price: ₹" + String.format(Locale.getDefault(), "%.2f", price), x + 250, detailsY, paint);
                    canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", itemTotal), x + 450, detailsY, paint);
                    detailsY += 15;
                    canvas.drawText("Qty: " + qty, x + 250, detailsY, paint);
                    
                    if (itemTaxTotal > 0) {
                        detailsY += 15;
                        canvas.drawText("Tax Amt: ₹" + String.format(Locale.getDefault(), "%.2f", itemTaxTotal), x + 250, detailsY, paint);
                    }
                    
                    // Ensure y is updated to the maximum of name end or details end
                    y = Math.max(y, detailsY) + 10;
                    canvas.drawLine(x, y, 555, y, getLinePaint());
                }
            }
        } catch (Exception e) {
            Log.e("InvoiceGenerator", "Error: " + e.getMessage());
        }

        y += 30;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Subtotal", x + 350, y, paint);
        canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", calculatedSubtotal), x + 450, y, paint);
        
        y += 20;
        canvas.drawText("Tax", x + 350, y, paint);
        canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", order.getTax()), x + 450, y, paint);

        // Coupon Section (Inside Totals)
        if (order.getCouponCode() != null && !order.getCouponCode().isEmpty()) {
            y += 20;
            canvas.drawText("Coupan:\n" + order.getCouponCode(), x + 350, y, paint);
            canvas.drawText("-₹" + String.format(Locale.getDefault(), "%.2f", order.getCouponDiscount()), x + 450, y, paint);
        }

        y += 15;
        canvas.drawLine(x + 350, y + 5, 555, y + 5, getLinePaint());
        
        y += 25;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Total", x + 350, y, paint);
        
        // Manual calculation of finalTotal to avoid 0.00 issue
        double finalTotal = calculatedSubtotal + order.getTax() - order.getCouponDiscount();
        canvas.drawText("₹" + String.format(Locale.getDefault(), "%.2f", finalTotal), x + 450, y, paint);

        y += 50;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Contact for more info : +91 9512017161 , sales@bulkagrochem.com", x, y, paint);

        pdfDocument.finishPage(page);

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "Invoice_" + order.getId() + ".pdf";
        File file = new File(directory, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "Invoice saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

    private static List<String> wrapText(String text, int width, TextPaint paint) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (paint.measureText(currentLine.toString() + " " + word) < width) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) lines.add(currentLine.toString());
        return lines;
    }

    private static Paint getHeaderPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#2E7D32"));
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    private static Paint getLinePaint() {
        Paint paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(1);
        return paint;
    }
}
