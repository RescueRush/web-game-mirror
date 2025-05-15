package lu.rescue_rush.game_backend.db.views.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.OrderBy;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.support.SupportFormData;
import lu.rescue_rush.game_backend.db.tables.support.SupportFormTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "support_form_count_per_email", tables = {
		@ViewTable(typeName = SupportFormTable.class, columns = {
				@ViewColumn(name = "email"),
				@ViewColumn(func = "count(id)", asName = "total_count")
		})
}, groupBy = "email",
orderBy = @OrderBy(column = "total_count", type = OrderBy.Type.DESC))
//@formatter:on
@Service
public class SupportForm_CountPerEmail_View extends R2DBView<SupportFormData> {

	@Autowired
	private SupportFormTable supportEmailForms;

	public SupportForm_CountPerEmail_View(DataBase dbTest) {
		super(dbTest);
	}

}
