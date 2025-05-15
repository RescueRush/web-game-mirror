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
@DB_View(name = "newsletter_subscription_api", tables = {
		@ViewTable(typeName = NewsletterSubscriptionTable.class, columns = {
				@ViewColumn(name = "id"),
				@ViewColumn(name = "email"),
				@ViewColumn(name = "since")
		})
}, condition = "`source` = 'api'",
orderBy = @OrderBy(column = "since", type = OrderBy.Type.ASC))
//@formatter:on
@Service
public class NewsletterSubscription_SourceApi_View extends R2DBView<NewsletterSubscriptionData> {

	@Autowired
	private NewsletterSubscriptionTable newsletterEmailTable;

	public NewsletterSubscription_SourceApi_View(DataBase dbTest) {
		super(dbTest);
	}

}
