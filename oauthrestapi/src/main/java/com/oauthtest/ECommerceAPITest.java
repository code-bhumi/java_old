package com.oauthtest;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import pojo.LoginRequest;
import pojo.LoginResponse;
import pojo.OrderDetail;
import pojo.Orders;

public class ECommerceAPITest {
    
    public static void main(String[] args) {

        RequestSpecification req = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
        .setContentType(ContentType.JSON).build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserEmail("siyaram_1234@gmail.com");
        loginRequest.setUserPassword("Siya@ram5");

        RequestSpecification reqLogin = given().log().all().spec(req).body(loginRequest);
        LoginResponse loginResponse = reqLogin.when().post("/api/ecom/auth/login")
        .then().log().all().extract().response().as(LoginResponse.class);
        System.out.println(loginResponse.getToken());
        String token = loginResponse.getToken();
        System.out.println(loginResponse.getUserId());
        String userId = loginResponse.getUserId();

        // Add product

        RequestSpecification addProductBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
        .addHeader("Authorization", token).build();

        RequestSpecification reqAddProduct = given().log().all().spec(addProductBaseReq)
        .param("productName", "Flower")
        .param("productAddedBy", userId)
        .param("productCategory", "decoration")
        .param("productSubCategory", "plants")
        .param("productPrice", "1100")
        .param("productDescription", "Orignal Roses")
        .param("productFor", "women")
        .multiPart("productImage",new File("//Users//meenakshipal//Documents//Rose.png"));

        String addProductResponse = reqAddProduct.when().post("/api/ecom/product/add-product")
        .then().log().all().extract().response().asString();
        JsonPath js = new JsonPath(addProductResponse);
        String productId  = js.get("productId");

        //  create Order
        RequestSpecification creatOrderBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
        .addHeader("Authorization", token).setContentType(ContentType.JSON).build();

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setCountry("India");
        orderDetail.setProductOrderId(productId);

        List <OrderDetail> orderDetailList = new ArrayList<OrderDetail> ();
        orderDetailList.add(orderDetail);

        Orders orders = new Orders();
        orders.setOrders(orderDetailList);

        RequestSpecification createOrderReq = given().log().all().spec(creatOrderBaseReq).body(orders);

        String responseAddOrder = createOrderReq.when().post("/api/ecom/order/create-order").then().log().all().extract().response().asString();
        System.out.println(responseAddOrder);

        // Delete product

        RequestSpecification deleteProductBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
        .addHeader("Authorization", token).setContentType(ContentType.JSON).build();

        RequestSpecification deleteProdReq = given().log().all().spec(deleteProductBaseReq).pathParam("productId", productId);
        String deleteProductResponse = deleteProdReq.when().delete("/api/ecom/product/delete-product/{productId}")
        .then().log().all().extract().response().asString();

        JsonPath js1 = new JsonPath(deleteProductResponse);
        Assert.assertEquals("Product Deleted Successfully", js1.get("message"));

    }
}
