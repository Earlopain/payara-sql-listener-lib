package net.c5h8no4na.sqllistener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FormattingTest {
	@Test
	void testSqlFormatting() {
		String input1 = "select tag0_.id as id1_8_, tag0_.tag_type_id as tag_type2_8_, tag0_.text as text3_8_ from tags tag0_ where tag0_.text in (? , ? , ? , ?)";
		String formatted1 = getFormatted(input1);
		String expected1 = "SELECT tags_0.id, tags_0.tag_type_id, tags_0.text FROM tags AS tags_0 WHERE tags_0.text IN (?)";
		Assertions.assertEquals(expected1, formatted1);

		String input2 = "select post0_.id as id1_6_0_, post0_.approver_id as approve16_6_0_, post0_.created_at as created_2_6_0_, post0_.description as descript3_6_0_, post0_.duration as duration4_6_0_, post0_.extension_id as extensio5_6_0_, post0_.fav_count as fav_coun6_6_0_, post0_.height as height7_6_0_, post0_.md5 as md8_6_0_, post0_.rating_id as rating_i9_6_0_, post0_.score_down as score_d10_6_0_, post0_.score_total as score_t11_6_0_, post0_.score_up as score_u12_6_0_, post0_.size as size13_6_0_, post0_.updated_at as updated14_6_0_, post0_.uploader_id as uploade17_6_0_, post0_.width as width15_6_0_, post0_1_.post_id as post_id1_2_0_, post1_.id as id1_6_1_, post1_.approver_id as approve16_6_1_, post1_.created_at as created_2_6_1_, post1_.description as descript3_6_1_, post1_.duration as duration4_6_1_, post1_.extension_id as extensio5_6_1_, post1_.fav_count as fav_coun6_6_1_, post1_.height as height7_6_1_, post1_.md5 as md8_6_1_, post1_.rating_id as rating_i9_6_1_, post1_.score_down as score_d10_6_1_, post1_.score_total as score_t11_6_1_, post1_.score_up as score_u12_6_1_, post1_.size as size13_6_1_, post1_.updated_at as updated14_6_1_, post1_.uploader_id as uploade17_6_1_, post1_.width as width15_6_1_, post1_1_.post_id as post_id1_2_1_ from posts post0_ left outer join post_children post0_1_ on post0_.id=post0_1_.child_id left outer join posts post1_ on post0_1_.post_id=post1_.id left outer join post_children post1_1_ on post1_.id=post1_1_.child_id where post0_.id= 100";
		String formatted2 = getFormatted(input2);
		String expected2 = "SELECT posts_0.id, posts_0.approver_id, posts_0.created_at, posts_0.description, posts_0.duration, posts_0.extension_id, posts_0.fav_count, posts_0.height, posts_0.md5, posts_0.rating_id, posts_0.score_down, posts_0.score_total, posts_0.score_up, posts_0.size, posts_0.updated_at, posts_0.uploader_id, posts_0.width, post_children_0.post_id, posts_1.id, posts_1.approver_id, posts_1.created_at, posts_1.description, posts_1.duration, posts_1.extension_id, posts_1.fav_count, posts_1.height, posts_1.md5, posts_1.rating_id, posts_1.score_down, posts_1.score_total, posts_1.score_up, posts_1.size, posts_1.updated_at, posts_1.uploader_id, posts_1.width, post_children_1.post_id FROM posts AS posts_0 LEFT OUTER JOIN post_children AS post_children_0 ON posts_0.id = post_children_0.child_id LEFT OUTER JOIN posts AS posts_1 ON post_children_0.post_id = posts_1.id LEFT OUTER JOIN post_children AS post_children_1 ON posts_1.id = post_children_1.child_id WHERE posts_0.id = 100";
		Assertions.assertEquals(expected2, formatted2);
	}

	private String getFormatted(String input) {
		return new SQLFormatter(input).prettyPrintNoFormatting();
	}
}
