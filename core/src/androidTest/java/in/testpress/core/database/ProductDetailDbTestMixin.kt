package `in`.testpress.core.database

import `in`.testpress.database.Course
import `in`.testpress.database.Orders
import `in`.testpress.database.ProductDetailEntity

open class ProductDetailDbTestMixin: DbTestMixin() {

    fun productDetailFixture(): ProductDetailEntity {
        return ProductDetailEntity(62,ordersFixture(),null,coursesFixture(),"surl",
                "title","furl",null,"slug","jul 1",null,"buy",
                null,"1234","jan 8")
    }

    private fun ordersFixture(): Orders {
        return Orders(null,66,"jan 5","check","1234","231",
                "api","1234567",null,"test",123,"Progress","123",
                "access","soft skill",null,"chennai","123",null,
                null,null,"data",true,"101","hash")
    }

    private fun coursesFixture(): List<Course?>? {
        return listOf(Course("img",12,"created","desc","soft skill",
                1,null,123,false,null,12,
                "url",null,null,1,"link",12,1,
                "slug",1,12,null))
    }

}