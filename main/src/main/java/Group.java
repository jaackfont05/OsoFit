import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * Created by: Ethan Rogers
 * Date created: 10-26-2025
 */
public class Group {
	public String name;
	public long id;
	private List<user> members;
	// implement messaging?

	private Random rand;
	
	
	
	public Group( String name, long id, List<user> users ) {
		this.name = name;
		this.id = id;
		members = users;
	}
	
	public Group( String name ) {
		this.name = name;
		id = rand.nextLong(); // check for dupliates in database
		members = new ArrayList<>();
	}
	
	public boolean containsUser( user u ) { return members.contains(u); }
	
	public void addUser( user u ) { members.add( u ); }
	public void removeUser( user u ) { members.remove( u ); }
}
