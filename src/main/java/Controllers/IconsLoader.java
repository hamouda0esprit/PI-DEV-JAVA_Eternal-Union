package Controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

public class IconsLoader {
    // Base64 data for the Facebook icon
    private static final String FACEBOOK_ICON_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAAXNSR0IArs4c6QAAAS1JREFUSEvNlYENwjAMBL+bwCTAJMAkwCTAJMAksAnoRIOcYIdWpVItVS3Fyb/fH7fRyNGMvL8mAbCUtJbEnXi096uks/ntilGrYCbpaDaO1NzXgCIA2F569OckaevlewAwvwebIwtBDhfBu1VExgOAedI7rYMdLG3sWhCXeUosAUr2NBR2qbE9VHunlgCwomkpeD703tUsKAHQPmkL67mzOc6qBYQ+FZcAT7MyArA5HlDWr1oFkTt+AWSy1gC8HvHOOx/WdZgi2fmryei76dnk0hgZ6bIC7wTT6Mimnq0zY3gHzTrJHjTKtkBUWjoqkyfSGFbonOxqnQKIHRP2P3ce1YYd7DwQz5rhPPrXuA5Pe5cvGo1ftAOQiugD1621Y3VOdQEYMoqm8U0eVMELTP07GR4htYYAAAAASUVORK5CYII=";
    
    // Base64 data for the webcam icon
    private static final String WEBCAM_ICON_BASE64 = "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgZmlsbD0iY3VycmVudENvbG9yIiBjbGFzcz0iYmkgYmktd2ViY2FtLWZpbGwiIHZpZXdCb3g9IjAgMCAxNiAxNiI+CiAgPHBhdGggZD0iTTYuNjQ0IDExLjA5NGEuNS41IDAgMCAxIC4zNTYtLjE1aDJhLjUuNSAwIDAgMSAuMzU2LjE1Yy4xNzUuMTc3LjM5LjM0Ny42MDMuNDk2YTcgNyAwIDAgMCAuNzUyLjQ1NmwuMDEuMDA2aC4wMDNBLjUuNSAwIDAgMSAxMC41IDEzaC01YS41LjUgMCAwIDEtLjIyNC0uOTQ3bC4wMDItLjAwMS4wMS0uMDA2YTQgNCAwIDAgMCAuMjE0LS4xMTYgOCA4IDAgMCAwIC41MzktLjM0Yy4yMTQtLjE1LjQyOC0uMzE5LjYwMy0uNDk2TTcgNi41YTEgMSAwIDEgMSAyIDAgMSAxIDAgMCAxLTIgMCIvPgogIDxwYXRoIGQ9Ik0yIDNhMiAyIDAgMCAwLTIgMnYzYTIgMiAwIDAgMCAyIDJoMTJhMiAyIDAgMCAwIDItMlY1YTIgMiAwIDAgMC0yLTJ6bTYgMS41YTIgMiAwIDEgMSAwIDQgMiAyIDAgMCAxIDAtNE0xMi41IDdhLjUuNSAwIDEgMSAwLTEgLjUuNSAwIDAgMSAwIDEiLz4KPC9zdmc+";
    
    // Base64 data for upload icon
    private static final String UPLOAD_ICON_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAAXNSR0IArs4c6QAAAdFJREFUSEvFlctR3EAQhv+WthwBdiwCw17lC5yDBHYVXC0bAZADJwJsIsCQgQ0R2Bzgy9bWQgSrCLAv9g1j92DW2kjbMCB8wPLRVdPT/XX/3dOthP/8k//MjwygEkfbAD4BmAPQBdCx9kQYIXrAI4AxgAsAt2bQGJQDVOJoGcB3AF9dwl+l1G/nnPEQA2ulls1cAPvmKrMN1QPUWrQJ4BvgY631QXryj03OLJzXg/0igCMAmxjsnjuOAWpxtA5g14TL5aWdXq8XlIB3Ot9+txYbgJ0h+lsZpATgZ1QkTdPLRqPRDQFKcxKEwFZdq9Xqq4zTM4BaHH0BcCZiuTUYDGZyQHbMZ/Ly7DRJklaapldmbMlTYLzWkL3fEoB7ABsRHUZR9N6TkARfeSILV0hKPm8G6EecmmZQkiQHURR9mOTdUTlOKZVm9SgAkElJmhJdw2BKKbXvvZ8DoJU6TdP0ZRFCAPLA84KrWPQ00Xh8HMdL2b3XWm9prS8dBHudvf0kcAlAOLLmGJJ/TZcpTXI2pAQgI/IQ/VTZC7ICcBbNXIWZfVBm0Rnmnt2m1QoX+UEXs8PpPJrGrOIslz0X/6UqKmv42bU4+gnghTlB5lNkAA8mdx/Rn4nX/AmAJ87Ibhgf8c1RAAAAAElFTkSuQmCC";
    
    // Base64 data for teach icon
    private static final String TEACH_ICON_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAAXNSR0IArs4c6QAAAjtJREFUSEu1lU9IVFEUxn/nzowoEUn0RyGEKBFcJFJEC4VEwSgXEREhkS5cuGgX5KpViyBp1UqIwFW0UCGEQiGiXSItIohAIogQGpXJKOadOMPMvHnv3Xtvxhnl3eV93/nud79zz5EsPvM/DRs+hx+PgF+BXcDe6PsZ8An4qGH4OotkboBw8eAU0E3y4QYY9MF7pQDC2cM7wNlEzMhgA0aA676G9lgA9+LBKxBHypKPQfSA7ynCR1r5UQQyB2Bn4BDwFjiQJjQz+AnsV4NveSCzABHEQVwtcS4rxu9SwxdFEGMAQaeCByB1J+8Af6Lx3QTsM4fNRvEDaugOA0wBeNwBGiA9DtxU1YaLWa4eQU/Q8MtxqAlgB+ACSV9ZB7YCv4H9avgxJ8iNQJhuXzP2wbaMmtFOw6cZi5wEXhbI3Zbh8KhxkYsBBLqDVzgXmlnLDqsZ+2mEt8nBNKX2Dkf9G6OIdwMX0pRMWvCDKk9qJnBHhUvuwbYzUBt+ApJr5/5jzapSqXqbZ01Q9ELN+JJuEK3yvWm9mgr4lc5XjTfA4ZzMTKrRriA+D7wuaHPf81eWDWB75QzweD7GnKDa0AKcwmW14F3UfxaVQbXhKsg1NZxLrbJJYXuVJtCihk9yAU4AS5M2dQO1YR/IkIbRPNgrZmvQ1Aql9SyA28DFooJPxZQGyAvKc+a0ADEL1nHdxmEjcTtv/ue14BLwADiUF4FdfwJ0azi9IdqNABwCzhDI7z8CBODFfPUlMQAAAABJRU5ErkJggg==";
    
    // Base64 data for learn icon
    private static final String LEARN_ICON_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAAXNSR0IArs4c6QAAAdJJREFUSEu1lc1RG0EQhWd2V1LWFWyRAZcr8AViEnAGhKAM2ETAkgPmxyQAZGAnAB9tDjYZ4CuuWnSiIUe2Vu0sO8sBWJV2evq9fq9nhlLw0YJ+Sh2gL8FXMDwC8A5AD8B30fgFwJnE+NzpcaoArxw8J8AVAFfV1gk2JcIoC5IJePHgDyJ8DJFWgrWmK8hA2BDgi+TfJQEPPHhD4Lw0YJkKSgMcM8HfHHxKhremlwU4BXBaBnDPg1ZKOcPc8lBDEI0sE8wYjH1qYN1iWwMCr92j2EvQKtpvD2BV7cjwbpIe7AVsqlE2tgkNLe/Mz/2PjYB56ksHTavwLr6fPQ+WO2n1oNrQOTgWJOcXK/BtVYU7HtxJWvxMWOIZgdHWWtN1kgQe67CrSQoLc1BRZTgF0LUt6eNXZfhKhDdpL/HFQU3o2g7q+4wy+jxJkglpEwFz1c/Lxt+7BZB2BO54s7xdBzXhU3SbAOxLPLFjExZUfgCw3/JTcmz+jh3LCEKUdibOKuewYckMk02qgzrbpKuU2FKtBu9Dc2sVWEq6GzwqxeAYwFH0yJ4QMAtVmEfBmvG+9aBF/Pp5D0LsAWgaG22qY19uP6X5HbCfnmOfyPG/1fcLg0dqGGFJrxQAAAAASUVORK5CYII=";
    
    public static void loadIcons() {
        try {
            // Save Facebook icon
            String facebookIconPath = "src/main/resources/Images/facebook.png";
            saveBase64AsImageFile(FACEBOOK_ICON_BASE64, facebookIconPath);
            
            // Save Webcam icon
            String webcamIconPath = "src/main/resources/Images/webcam.png";
            saveBase64AsImageFile(WEBCAM_ICON_BASE64, webcamIconPath);
            
            // Save Upload icon
            String uploadIconPath = "src/main/resources/Images/upload-icon.png";
            saveBase64AsImageFile(UPLOAD_ICON_BASE64, uploadIconPath);
            
            // Save Teach icon
            String teachIconPath = "src/main/resources/Images/teachi.png";
            saveBase64AsImageFile(TEACH_ICON_BASE64, teachIconPath);
            
            // Save Learn icon
            String learnIconPath = "src/main/resources/Images/mo.png";
            saveBase64AsImageFile(LEARN_ICON_BASE64, learnIconPath);
            
            System.out.println("Icons loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading icons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void saveBase64AsImageFile(String base64String, String filePath) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            
            File file = new File(filePath);
            // Create parent directory if it doesn't exist
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decodedBytes);
            }
            
            System.out.println("Image saved to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 