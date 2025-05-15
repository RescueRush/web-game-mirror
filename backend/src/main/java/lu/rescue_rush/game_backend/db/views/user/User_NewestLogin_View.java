package lu.rescue_rush.game_backend.db.views.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.OrderBy;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "user_newest_login", tables = {
		@ViewTable(typeName = UserTable.class, columns = {
				@ViewColumn(name = "id"),
				@ViewColumn(name = "name"),
				@ViewColumn(name = "email"),
				@ViewColumn(func = "DATE(last_login)", asName = "last_login")
		})
}, orderBy = { @OrderBy(column = "last_login", type = OrderBy.Type.DESC) })
//@formatter:on
@Service
public class User_NewestLogin_View extends R2DBView<UserData> {

	@Autowired
	private UserTable users;

	public User_NewestLogin_View(DataBase dbTest) {
		super(dbTest);
	}

}
