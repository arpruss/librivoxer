package mobi.omegacentauri.Librivoxer;

public interface BookSaver {
	public void saveStart();
	public void saveBook(Book book);
	public void saveDone();	
}
