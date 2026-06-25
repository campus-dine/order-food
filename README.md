# order-food
校园点餐服务后端项目
controller层测试均成功

但是UserController,logininterceptor和JwtTokenUtilTest的测试未通过，可能是由于以下原因：
1.需要 Token 解析成功的业务逻辑,使用集成测试（@SpringBootTest）配合真实的 JWT Token,或者将 Token 解析逻辑移到拦截器或过滤器中
将 Token 解析逻辑移到独立的 Service 类（非静态方法