Review scraper from Google Maps.

## Installation
1. clone repository.
<pre><code>git clone https://github.com/lijm1358/GoogleMapsReviewScraper.git</code></pre>
2. install ChromeDriver and unzip inside of directory.  (https://chromedriver.chromium.org/downloads). check version of Chrome before download ChromeDriver.
3. install requirements using pip.
<pre><code>pip install -r requirements.txt</code></pre>

## Usage
```
python mapscraper -m [MODE] -i [FILE or STRING]

  -m [MODE], --mode [MODE]
      set mode of scraper. 4 strings are available.
        reviewer : get all reviewers from input string which indicates place
        reviewer_fromlist : get all reviewers from input file. file must be list of places you want to search for
        review : get all reviews from input string which indicates place
        review_fromlist : get all reviews from input file. file must be list of places you want to search for

  -i [FILE or STRING], --input [FILE or STRING]
      input of scraper. must be string or filename.
      [reviewer, review] needs input as string, [reviewer_fromlist, review_fromlist] needs input as filename.
```

ex) if you want to get all reviews from 서면역, type like this:
<pre><code>python mapscraper.py -m review -i "서면역"</code></pre>