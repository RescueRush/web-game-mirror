package lu.rescue_rush.game_backend.db.views.newsletter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.OrderBy;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.newsletter.NewsletterSubscriptionData;
import lu.rescue_rush.game_backend.db.tables.newsletter.NewsletterSubscriptionTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "newsletter_subscription_count_per_source", tables = {
		@ViewTable(typeName = NewsletterSubscriptionTable.class, columns = {
				@ViewColumn(func = "count(*)", asName = "total_count"),
				@ViewColumn(func = "count(CASE WHEN `email` IS NOT NULL THEN 1 END)", asName = "subscribed_count"),
				@ViewColumn(func = "count(CASE WHEN `email` IS NOT NULL THEN 1 END) / NULLIF(count(*), 0)", asName = "subscribed_ratio"),
				@ViewColumn(name = "source")
		})
}, groupBy = "source",
orderBy = @OrderBy(column = "total_count", type = OrderBy.Type.DESC))
//@formatter:on
@Service
public class NewsletterSubscription_CountPerSource_View extends R2DBView<NewsletterSubscriptionData> {

	@Autowired
	private NewsletterSubscriptionTable newsletterEmailTable;

	public NewsletterSubscription_CountPerSource_View(DataBase dbTest) {
		super(dbTest);
	}

}
