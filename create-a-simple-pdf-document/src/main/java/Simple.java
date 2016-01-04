import net.contargo.print.pdf.PDFBuilder;

public class Simple {

	public static void main(String[] args) throws Exception {
		
		PDFBuilder.fromTemplate(System.in)
			.withReplacement("@name@", "World")
			.build()
			.save(System.out);
	}
}
