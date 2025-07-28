/**
 * Firebase Cloud Function for sending emails
 * This function processes email queue documents and sends emails using a service like SendGrid, Mailgun, or Gmail API
 * 
 * To deploy this function:
 * 1. Install Firebase CLI: npm install -g firebase-tools
 * 2. Initialize functions: firebase init functions
 * 3. Add this code to functions/index.js
 * 4. Install dependencies: npm install @sendgrid/mail
 * 5. Deploy: firebase deploy --only functions
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize admin only if not already initialized
if (!admin.apps.length) {
    admin.initializeApp();
}

// Import SendGrid only if API key is available
let sgMail = null;
try {
    sgMail = require('@sendgrid/mail');
    const apiKey = functions.config().sendgrid?.apikey;
    if (apiKey) {
        sgMail.setApiKey(apiKey);
        console.log('SendGrid initialized successfully');
    } else {
        console.warn('SendGrid API key not found in Firebase config');
    }
} catch (error) {
    console.error('Failed to initialize SendGrid:', error);
}

/**
 * Cloud Function triggered when a new email is added to the emailQueue collection
 */
exports.processEmailQueue = functions.firestore
    .document('emailQueue/{emailId}')
    .onCreate(async (snap, context) => {
        const emailData = snap.data();
        
        try {
            console.log('Processing email:', context.params.emailId);
            console.log('Email data:', JSON.stringify(emailData, null, 2));
            
            if (emailData.processed) {
                console.log('Email already processed, skipping');
                return null;
            }

            // Check if SendGrid is available
            if (!sgMail) {
                console.error('SendGrid not initialized - marking email as failed');
                await snap.ref.update({
                    processed: true,
                    processedAt: admin.firestore.FieldValue.serverTimestamp(),
                    status: 'failed',
                    error: 'SendGrid not initialized'
                });
                return null;
            }
            
            // Prepare email content based on template
            let htmlContent = '';
            let textContent = '';
            
            if (emailData.template === 'contact_seller') {
                htmlContent = generateContactSellerEmail(emailData.templateData);
                textContent = generateContactSellerTextEmail(emailData.templateData);
            } else if (emailData.template === 'welcome') {
                htmlContent = generateWelcomeEmail(emailData.templateData);
                textContent = generateWelcomeTextEmail(emailData.templateData);
            } else {
                throw new Error(`Unknown email template: ${emailData.template}`);
            }
            
            // Send email using SendGrid
            const msg = {
                to: emailData.to,
                from: 'noreply@giggo.com', // Your verified sender email
                subject: emailData.subject,
                text: textContent,
                html: htmlContent,
            };
            
            console.log('Sending email to:', emailData.to);
            await sgMail.send(msg);
            console.log('Email sent successfully to:', emailData.to);
            
            // Mark email as processed
            await snap.ref.update({
                processed: true,
                processedAt: admin.firestore.FieldValue.serverTimestamp(),
                status: 'sent'
            });
            
        } catch (error) {
            console.error('Error sending email:', error);
            
            // Mark email as failed
            await snap.ref.update({
                processed: true,
                processedAt: admin.firestore.FieldValue.serverTimestamp(),
                status: 'failed',
                error: error.message
            });
        }
        
        return null;
    });

/**
 * Generate HTML email for contact seller notification
 */
function generateContactSellerEmail(data) {
    const contactDate = new Date(data.contactDate).toLocaleDateString();
    
    return `
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            .container { background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
            .header { text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #6366f1; }
            .logo { font-size: 28px; font-weight: bold; color: #6366f1; margin-bottom: 10px; }
            .title { color: #6366f1; font-size: 24px; margin-bottom: 20px; }
            .gig-info { background-color: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #6366f1; }
            .buyer-info { background-color: #f0fdf4; padding: 15px; border-radius: 8px; margin: 15px 0; }
            .message-box { background-color: #fffbeb; padding: 15px; border-radius: 8px; margin: 15px 0; border: 1px solid #fbbf24; }
            .button { display: inline-block; background-color: #6366f1; color: white; padding: 12px 25px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
            .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 14px; }
            .highlight { color: #6366f1; font-weight: bold; }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <div class="logo">🚀 GigGO</div>
                <p>Your Freelance Marketplace</p>
            </div>
            
            <h2 class="title">🔔 New Contact Request!</h2>
            
            <p>Hello there,</p>
            
            <p>Great news! Someone is interested in your gig on GigGO. Here are the details:</p>
            
            <div class="gig-info">
                <h3>📋 Gig Details</h3>
                <p><strong>Gig Title:</strong> <span class="highlight">${data.gigTitle}</span></p>
                <p><strong>Gig ID:</strong> ${data.gigId}</p>
            </div>
            
            <div class="buyer-info">
                <h3>👤 Buyer Information</h3>
                <p><strong>Name:</strong> <span class="highlight">${data.buyerName}</span></p>
                <p><strong>Email:</strong> ${data.buyerEmail}</p>
                <p><strong>Contact Date:</strong> ${contactDate}</p>
            </div>
            
            ${data.message ? `
            <div class="message-box">
                <h3>💬 Personal Message</h3>
                <p><em>"${data.message}"</em></p>
            </div>
            ` : ''}
            
            <p>🎉 This is a great opportunity to connect with a potential client!</p>
            
            <div style="text-align: center;">
                <a href="https://giggo.app/notifications" class="button">View in GigGO App 📱</a>
            </div>
            
            <div style="margin: 25px 0; padding: 15px; background-color: #f3f4f6; border-radius: 6px;">
                <h4>💡 Next Steps:</h4>
                <ol>
                    <li>Review the buyer's request carefully</li>
                    <li>Respond promptly to show professionalism</li>
                    <li>Ask clarifying questions if needed</li>
                    <li>Provide a detailed quote and timeline</li>
                </ol>
            </div>
            
            <div class="footer">
                <p>This email was sent automatically from <strong>${data.appName}</strong></p>
                <p>© 2025 GigGO. All rights reserved.</p>
                <p>If you have any questions, please contact our support team.</p>
            </div>
        </div>
    </body>
    </html>
    `;
}

/**
 * Generate text email for contact seller notification
 */
function generateContactSellerTextEmail(data) {
    const contactDate = new Date(data.contactDate).toLocaleDateString();
    
    return `
🚀 GigGO - New Contact Request!

Hello there,

Great news! Someone is interested in your gig on GigGO.

📋 Gig Details:
- Gig Title: ${data.gigTitle}
- Gig ID: ${data.gigId}

👤 Buyer Information:
- Name: ${data.buyerName}
- Email: ${data.buyerEmail}
- Contact Date: ${contactDate}

${data.message ? `💬 Personal Message:\n"${data.message}"\n` : ''}

🎉 This is a great opportunity to connect with a potential client!

💡 Next Steps:
1. Review the buyer's request carefully
2. Respond promptly to show professionalism
3. Ask clarifying questions if needed
4. Provide a detailed quote and timeline

View this in your GigGO app: https://giggo.app/notifications

---
This email was sent automatically from ${data.appName}
© 2025 GigGO. All rights reserved.
    `;
}

/**
 * Generate welcome email HTML
 */
function generateWelcomeEmail(data) {
    return `
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
            .container { background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
            .header { text-align: center; margin-bottom: 30px; }
            .logo { font-size: 28px; font-weight: bold; color: #6366f1; margin-bottom: 10px; }
            .button { display: inline-block; background-color: #6366f1; color: white; padding: 12px 25px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <div class="logo">🚀 GigGO</div>
                <h2>Welcome to the Future of Freelancing!</h2>
            </div>
            
            <p>Hello ${data.userName},</p>
            
            <p>Welcome to GigGO! We're excited to have you join our growing community of freelancers and clients.</p>
            
            <p>🎉 Your journey to finding amazing gigs or talented freelancers starts now!</p>
            
            <div style="text-align: center;">
                <a href="https://giggo.app" class="button">Start Exploring 🚀</a>
            </div>
            
            <p>If you have any questions, feel free to reach out to us at ${data.supportEmail}</p>
            
            <p>Happy freelancing!<br>The GigGO Team</p>
        </div>
    </body>
    </html>
    `;
}

/**
 * Generate welcome email text
 */
function generateWelcomeTextEmail(data) {
    return `
🚀 Welcome to GigGO!

Hello ${data.userName},

Welcome to GigGO! We're excited to have you join our growing community of freelancers and clients.

🎉 Your journey to finding amazing gigs or talented freelancers starts now!

Visit us at: https://giggo.app

If you have any questions, feel free to reach out to us at ${data.supportEmail}

Happy freelancing!
The GigGO Team
    `;
}
