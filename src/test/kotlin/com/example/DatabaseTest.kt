package com.example

class DatabaseTest {

//    private val db = Database.connect(
//        url = "jdbc:h2:mem:test",
//        driver = "org.h2.Driver",
//        user = "root",
//        password = ""
//    )
//
//    @Test
//    fun useDsl() {
//        transaction(db) {
//            addLogger(StdOutSqlLogger)
//            SchemaUtils.create(Posts)
//
//            val insertedAuthor = Authors.insertAndGetId {
//                it[name] = "Anthony"
//            }
//
//            val author01 = Authors.insert {
//                it[name] = "Anthony"
//            }
//
//            val inserted = Posts.insert {
//                it[title] = "New Life"
//                it[content] = "Your new life need to renewal."
//                it[status] = PostStatus.DRAFT
//                // it[author] = author01[Authors.id]
//                it[author] = insertedAuthor
//            }
//            assertEquals(inserted.insertedCount, 1)
//            assertEquals(Posts.select(Posts.id).count(), 1L)
//            val postLists = Posts.selectAll().where {
//                Op.build { Posts.title eq "New Life" }
//            }.toList()
//            assertEquals(postLists.size, 1)
//            val post: ResultRow = postLists[0]
//            assertEquals(post[Posts.title], "New Life")
//        }
//    }
//
//    @Test
//    fun useDao() {
//        transaction(db) {
//            addLogger(StdOutSqlLogger)
//            SchemaUtils.create(Posts)
//
//            val author01 = Author.new {
//                name = "Bailey"
//            }
//
//            val post01 = Post.new {
//                title = "Second Life"
//                content = "Your new life don't need to anymore"
//                status = PostStatus.DRAFT
//                author = author01
//            }
//            assertEquals(Posts.select(Posts.id).count(), 1L)
//            val postLists = Posts.selectAll().where {
//                Op.build { Posts.title eq post01.title }
//            }.toList()
//            assertEquals(postLists.size, 1)
//            val postByRead = Post.wrapRow(postLists[0])
//            assertEquals(postByRead.title, post01.title)
//            assertEquals(postByRead.description(), "${postByRead.title} by ${postByRead.author}")
//        }
//    }
}
