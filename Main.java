

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	
	public static Pattern idPattern = Pattern.compile("<Id>(.+?)</Id>");
	public static Pattern titlePattern = Pattern.compile("<ArticleTitle>(.+?)</ArticleTitle>", Pattern.DOTALL);
	public static Pattern abstractPattern = Pattern.compile("<AbstractText.*?>(.+?)</AbstractText>", Pattern.DOTALL);
	public static Pattern redirectPattern = Pattern.compile("<a href=\"(.+?)\">here</a>");
	public static Pattern webPattern = Pattern.compile("<WebEnv>(.+?)</WebEnv>");
	public static Pattern keyPattern = Pattern.compile("<QueryKey>(.+?)</QueryKey>");
	public static Pattern countPattern = Pattern.compile("<Count>(.+?)</Count>");
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		// String outputDir = "/Users/feili/resource/pubmed";
		String outputDir = args[0];
		HashSet<String> existID = new HashSet<>();
		int retmax = Integer.parseInt(args[2]);
		int max_for_each_item = Integer.parseInt(args[3]);
		
		File folder = new File(outputDir);

	    if (folder.exists()==false) {
	    	folder.mkdirs();
	    } else {
	    	File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				String filename = listOfFiles[i].getName();
				String id = filename.substring(0, filename.indexOf(".txt"));
				existID.add(id);
			}
	    }
		
		// File entityFile = new File("/Users/feili/eclipse-workspace/pubmedCrawler/src/key_word.txt");
		File entityFile = new File(args[1]);
		
		BufferedReader br = new BufferedReader(new FileReader(entityFile));
		String line = null;
		int ct_line = 1;
		boolean start = false;
		int begin_line = Integer.parseInt(args[4]);
		
		while((line = br.readLine())!=null){

			if (ct_line == begin_line)
				start = true;
			
			if (start == false) {
				ct_line ++;
				continue;
			}
			
			if (line.contains("Retired procedure")) { // special for snomed
				ct_line ++;
				continue;
			}
			
			System.out.println("#### begin feching "+ct_line+" "+line);

			line = line.replaceAll("\\s", "+");
/*			String urlFindID = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?"
					+ "db=pubmed&term="+line+"&retmode=xml";*/
			String urlFindID = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?"
					+ "db=pubmed&term="+line+"&retmode=xml&usehistory=y";
			String ret1 = getURLContent(urlFindID);
			System.out.println(ret1);
			
			Matcher m1 = countPattern.matcher(ret1);
			int count = 0;
			if (m1.find())
				count = Integer.parseInt(m1.group(1));
			else
				throw new RuntimeException("can't find count");
			
			if (count == 0) {
				ct_line ++;
				continue;
			}
				
			m1 = webPattern.matcher(ret1);
			String web = null;
			if (m1.find()) {
				
				web = m1.group(1);
				
				m1 = keyPattern.matcher(ret1);
				String key = null;
				if (m1.find())
					key = m1.group(1);
				else
					throw new RuntimeException("can't find key");
				
				
				int already_get_for_each_item = 0;
						
				for (int retstart = 0; retstart < count; retstart += retmax) {
					
					if (already_get_for_each_item >= max_for_each_item) 
						break;
					
					String urlGetByBatch = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?"
							+ "db=pubmed&retmode=text&rettype=xml"
							+ "&WebEnv="+web+"&query_key="+key+"&retmax="+retmax
							+ "&retstart="+retstart;
					
					String ret2 = getURLContent(urlGetByBatch);
					System.out.println(ret2);
					
					
					Matcher matcher = idPattern.matcher(ret2);
					
					while(matcher.find()) {
						
						if (already_get_for_each_item >= max_for_each_item) 
							break;

						String id = matcher.group(1);
		
						if(existID.contains(id))
							continue;
						else
							existID.add(id);
						
						String urlGetByID = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"
								+ "db=pubmed&id="+id+"&retmode=text&rettype=xml";
						String contentGetByID = getURLContent(urlGetByID);

						String ret3 = id+"|t|";
						Matcher title_matcher = titlePattern.matcher(contentGetByID);
						if(title_matcher.find()) {
							ret3 += title_matcher.group(1);
						} else {
							System.out.println("can't find title: "+id);
							continue;
						}
							
						ret3 += "\n";
						
						ret3 += id+"|a|";
						Matcher abstract_matcher = abstractPattern.matcher(contentGetByID);
						boolean findAtLeastOnce = false;
						while(abstract_matcher.find()) {
							ret3 += abstract_matcher.group(1);
							findAtLeastOnce = true;
						} 
						if (findAtLeastOnce == false) {
							System.out.println("can't find abstract: "+id);
							continue;
						}
						ret3 += "\n";
		
		
						OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputDir+"/"+id+".txt"), "utf-8");
						
						osw.write(ret3);
						osw.close();
						
						already_get_for_each_item++;
		        
					}
			        
				}
			}
			else {
				System.out.println("can't find web, try to use id");
				
				int already_get_for_each_item = 0;
				
				Matcher matcher = idPattern.matcher(ret1);
				
				while(matcher.find()) {
					
					if (already_get_for_each_item >= max_for_each_item) 
						break;

					String id = matcher.group(1);
	
					if(existID.contains(id))
						continue;
					else
						existID.add(id);
					
					String urlGetByID = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"
							+ "db=pubmed&id="+id+"&retmode=text&rettype=xml";
					String contentGetByID = getURLContent(urlGetByID);

					String ret3 = id+"|t|";
					Matcher title_matcher = titlePattern.matcher(contentGetByID);
					if(title_matcher.find()) {
						ret3 += title_matcher.group(1);
					} else {
						System.out.println("can't find title: "+id);
						continue;
					}
						
					ret3 += "\n";
					
					ret3 += id+"|a|";
					Matcher abstract_matcher = abstractPattern.matcher(contentGetByID);
					boolean findAtLeastOnce = false;
					while(abstract_matcher.find()) {
						ret3 += abstract_matcher.group(1);
						findAtLeastOnce = true;
					} 
					if (findAtLeastOnce == false) {
						System.out.println("can't find abstract: "+id);
						continue;
					}
					ret3 += "\n";
	
	
					OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputDir+"/"+id+".txt"), "utf-8");
					
					osw.write(ret3);
					osw.close();
					
					already_get_for_each_item++;
	        
				}
				
			}
			
			System.out.println("#### end feching "+ct_line+" "+line);
			ct_line++;
		}

		br.close();

	}
	
	
	public static String getURLContent(String url) {
		URL u = null;
		URLConnection uc = null;
		BufferedReader reader = null;
		String s = "";
		int tryCount = 0;
		//while (tryCount<5) {
		while (true) {
			try {
				tryCount++;
				u = new URL(url);
				uc = u.openConnection();
				uc.setConnectTimeout(5000);
				uc.setReadTimeout(5000);  
				uc.connect();  
				reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(uc.getInputStream())));
				int c = 0;
				while ((c = reader.read()) != -1) {
					s += (char)c;
				}
				reader.close();
				break;
			} catch (Exception e) {
				System.out.println("error: "+url);
				System.out.println("try "+tryCount+" times");
				try {
					Thread.sleep(6000);
				} catch(Exception ee) {
					System.out.println("error: sleep");
				}
				continue;
			}			
		}

		return s;
	}

}
