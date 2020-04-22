# C-3PO

C-3PO is a Java-based static web site generator.

First and foremost C-3PO is based on the [Thymeleaf 2.1.3](http://www.thymeleaf.org/doc/tutorials/2.1/usingthymeleaf.html)
templating system. Thymeleaf is a template engine (like JSP) but also
involves good layout support (like Tiles).


## Requirements

C-3PO requires a Java 11 JRE installed on you computer. Upon installation you
also need Gradle installed. C-3PO has been tested with Gradle 3.0 and 2.12.


## Setup

At the moment only installing from source is supported. Follow these steps

- ensure a Java SE JDK 11 is installed on your computer
- ensure Gradle (version 3.0, version 2.12 should be fine too) is installed on your computer
- clone C-3PO's repository to your machine
- in the repository's root directory call `gradle installDist`
- add the directory **_build/install/C-3PO/bin_** to your **PATH**


## Usage

C-3PO is a command line tool, both for Windows and Unix. C-3PO accepts these command line paramters:

- -src <dir-name> ... the root source directory of your website
- -dest <dir-name> ... the root destination directory in which the website should be generated into
- -a ... if the flag is set, C-3PO builds the website as soon as files have changed in the source directory tree. This is a useful option when fiddling around with CSS for example.

**Heads up!** C-3PO is preventing you from accidentally using the same `src` and `dest` directories because this would mean that the source files would be overwritten by their generated counterparts.

Here's an example

```
c-3po -src . -dest site -a
```

For each file within the project directory structure C-3PO decides what to
do with it. At the moment C-3PO can

- process Thymeleaf based HTML5 files
  - Thymeleaf's [layout dialect](http://www.thymeleaf.org/doc/articles/layouts.html) is enabled
  - Thymeleaf's `LEGACYHTML5` template mode is enabled
- copy static resources like CSS and JS files into the destination directory

C-3PO does not require a certain project structure.
However, it is recommended to follow well-established standards. Here's an
example:

- *css/* --> your own CSS or SASS stylesheets
- *css/vendor* --> thired-party CSS stylesheets
- *js/* --> your own JavaScript files
- *js/vendor/* --> third-party JavaScript files
- *img/* --> image files


### Samples
C-3PO comes with a sample website that would server as a good starting point for creating a new website. Take a look at `samples/base-website`.
The sample website illustrates how to:
- configure `.c3poignore` to ignore certain files
- configure `.c3posettings` to trigger convenience functions like creating a sitemap.xml file
- show how to use Thymeleaf's decorator-based layout system (Thymeleaf layout dialect)
- show how to use **markdown** to created web pages faster and in a more convenient way
- include useful stuff for web development like **CSS resets** and a **HTML5 shiv** takes makes older browsers recognize new HTML5 elements
Building the sample website is done like this: `c-3po -src samples/base-website -dest <your-dir-of-choice>`.

### Settings

C-3PO looks for a **.c3posettings** file in the top-level source directory. It's a Java standard properties file
holding configuration preferences.

Here is a list of available settings:

- baseUrl ... the base URL of the deployed website. If not set, C-3PO does not generate a sitemap.xml file.


### Generating sitemap.xml and robots.txt

C-3PO is able to generate a `sitemap.xml` (as specified at http://www.sitemaps.org) file and a `robots.txt` file.
In order to generate a sitemap.xml, C-3PO requires **two prerequisites** to be fulfilled:

- there must not exist a sitemap.xml file in the source directory (the same is true for robots.txt)
- the **baseUrl** (e.g. http://yodaconditions.net) setting must be set in `.c3posettings`

Sometimes it's also desirable to exclude URLs from being crawled. This can be done in the `.c3poignore` file as described in the corresponding section.

When there is no `robots.txt` in the source folder, the C-3PO generates a minimal one putting the URL of sitemap.xml into it. This gives search crawlers a hint where to look for a sitemap file.

**Heads up!** Generation of sitemap.xml and robots.txt is not supported in *autoBuild* mode.

### Ignoring certain files

You'll want to ignore certain files, e.g. the .git folder. Place a text file
named **.c3poignore** into the root directory (defined by -src). Therein list
the files and directories (one line for each) that should not be
processed by C-3PO.

#### Can I use wildcards?

Yes. The [glob](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob)
syntax is supported.

#### .c3poignore example

```
.git
.idea
learnings.md
tasks.md
.editorconfig
.gitattributes
.gitignore
*.sample
```

#### Ignoring files for sitemap generation only

As already mentioned in the **sitemap generation** section, C-3PO allows to ignore files and directories only in the context of sitemap generation. Simply place `es` (*exlude in sitemap*) within square brackets `[...]` after an entry of `.c3poignore`.

In the following example the directory `private` is only excluded in terms of sitemap generation:

```
.git
.idea
tasks.md
private [es]
```

#### Ignoring output of files for destination folder

Most likely you'll have a Thymeleaf layout file somewhere in the project that is an important part of the end result but that should not end up in the build directory itself. Given a `_layouts/main-layout.html` file you can exclude it from the build directory with the `[er]` modifier like this:

```
.git
.idea
tasks.md
private [es]
_layouts [er]
```

**Heads Up!** Those files and directories still trigger a build when being modified in *autoBuild* mode.


### Using Markdown
C-3PO allows you to write in markdown. To be precise [commonmark](http://commonmark.org/) is used. Why? Because it's an effort to standardize markdown syntax.

**Anyways, how do you use markdown with C-3PO?**

Create a markdown file. Then create a Thymeleaf template called `md-template.html` in the same directory. Within `md-template.html` you are able to access two `context` objects called `markdownContent` (the HTML elements that result from processing the markdown file as a `String`) and `markdownHead` (an object representing `meta` tags and `title` to be included in the page's `head`; further description below). `md-template.html` is simply a wrapper for the markdown content that allows us to integrate with the site's layout and so on.

**Heads up!** If C-3PO does not find a file called `md-template.html` in the directory, it will not process the markdown file.

**Heads up!** C-3PO does not do HTML escaping of markdown content itself since markdown allows to have inline HTML markup like `<cite>` in markdown text.

Example of a `md-template.html` file:

```
<!DOCTYPE html>
<html layout:decorator="_layouts/main-layout">
	<head>
		<title th:text="${markdownHead.title}"></title>
		<meta th:each="metaTagEntry : ${markdownHead.metaTags}"
			th:name="${metaTagEntry.key}"
			th:content="${metaTagEntry.value}">
	</head>
	<body>
		<div layout:fragment="content">
			<div th:utext="${markdownContent}">
				This is replaced by markdown based blog content.
			</div>
	</body>
</html>
```

Note the use of `th:utext` to spit out the HTML string in `markdownContent`. Beware that `th:utext` renders **unescaped** text.

#### Define HTML title and meta tags in markdown
C-3PO introduced an extension to commonmark allowing editors to define the `title` and `meta tags` for the resulting HTML page.

**Why is this useful?**

1. Reader Experience: people like when browser tabs show meaningful titles
2. SERPs: the contents of the `meta description` tag is shown on *search engine result pages (SERP)*. Ideally a description is 150 to 160 characters long. A good meta description will raise the chances that search engine users click through to your site.

**So, how do I define these meta tags?**

```
$meta-title: A catchy page title
$meta-description: A summary that describes the contents (ideally 150 to 160 characters)

# Some heading
...
```

They must start with `$meta-`. Everything between `$meta-` and the colon `:` will become the name of the meta tag. The rest after the colon `:` will be the content of the meta tag.

When processing a markdown file, C-3PO will put this data as an object called `markdownHead` into the template's context. You'll be able to use the `markdownHead` object in the Thymeleaf template file `md-template.html` like this:

```
<title th:text="${markdownHead.title}"></title>
<meta th:each="metaTagEntry : ${markdownHead.metaTags}"
  th:name="${metaTagEntry.key}"
  th:content="${metaTagEntry.value}">
```

#### Access the name of the markdown file
In some cases you'll want to access the name of the markdown file, that is being processed, in your templates (i.e. layout templates). You might know that Thymeleaf passes the name of the current template being processed to `${execInfo.templateName}`. But if a markdown file is processed, the template in use is by convention `md-template.html`, meaning that `${execInfo.templateName}` basically resolves to `md-template.html`. But for some situations you'll want to know the name of the markdown file that's being wrapped by `md-template.html`. C-3PO provides that in `markdownFileName` that you can access with `${markdownFileName}`.

**Heads up!** Before using it in your templates, you probably want to check if it is even set (e.g. when mixing markdown and html content). Here's an expression that does that: `${markdownFileName} != null`.

### Using SASS / SCSS
C-3PO is able to process **SASS / SCSS** stylesheets. SASS / SCSS is a **CSS preprocessor** and enables you to use useful things
like **selector nesting** or **variables** in your stylesheets. Read more about it at <http://sass-lang.com>.

Beware that there's a difference between `.scss` and `.sass` files. E.g. in `.sass` files you can omit curly brackets and the
indentation level of your code is important. However, seemingly the SASS owners introduced SCSS later on because
SASS was causing some confusion.

Note: C-3PO is minifying CSS output by default.

See the sample project **samples/base-website** for a basic SASS example.


## FAQ for Website Editing

### When using Thymeleaf's Layout dialect how to pass parameters from a template to its layout?
When you use a decorator-based layout, you may want to pass parameters from
the content template to the layout template. To accomplish this simply use
`th:with` in the content template like this:

```
<html layout:decorator="_layouts/main-layout" th:with="activeMainNavEntry='home'">
```

This is useful for example when
the main navigation is defined in the layout page and you want to control
which navigation entry is rendered as active.

### How to control which navigation entry is active when using Thymeleaf's Layout dialect?
Assuming you're using Thymeleaf's [Layout dialect](https://github.com/ultraq/thymeleaf-layout-dialect)
in conjunction with decorator-based layouts there are two possibilities:

- Evaluate the standard `${execInfo.templateName}` in the layout template to determine
which template is currently being decorated. This is useful for smaller sites,
probably without a sub navigation.
- Pass a parameter, for example `activeMainNavEntry`, to the layout template and
and evaluate it when rendering the navigation. This is more useful for larger
sites where you want to control a main and a sub-navigation and the name of
the template currently being decorated doesn't reflect the main navigation category
that is supposed to be shown as active.

### How to avoid obsolete &lt;div&gt; elements resulting from a decorator-based layout (layout dialect)?
Usually you do something like this in your `layout.html`

```
...
<div layout:fragment="content">
  <!-- This will be replaced by content from content template -->
</div>
...
```

... and in your content template `content1.html` something like this

```
...
<div layout:fragment="content">
  <h1>Foo Bar</h1>
</div>
...
```

But what if you want to include page-specific scripts that way? The `div` elements would surround the `script` declarations. In this case just replace `div` elements with `th:block` elements in both the `decorator` (the layout) and the `content template` (the content page).

## Development

### How to build C-3PO?

C-3PO builds with [Gradle 3.0](https://docs.gradle.org/3.0/userguide/userguide.html) and uses its [application plugin](https://docs.gradle.org/3.0/userguide/application_plugin.html).

- *gradle build* ... builds (compile, test etc.) the project
- *gradle distZip* ... creates a ZIP-packaged distribution of C-3PO
- *gradle installDist* ... installs C-3PO into *build/install*

**Hint**: you can put the **/bin** directory within the install directory to your operating system's search **PATH**. This way C-3PO will always be available on the command line.
This is very useful when developing C-3PO and building a website with C-3PO at the same time.

### Solution log

#### Decide which absolute asset URLs to consider when fingerprinting assets, 2020-04-22
When fingerprinting an asset, which hostnames are considered to be the same when it comes to replacing an absolute URL to this asset with its fingerprinted counterpart? Suppose the base URL is https://example.com. Should https://www.example.com be considered the same? For example, should https://www.example.com/css/main.css be replaced by https://www.example.com/css/main.<fingerprint>.css given the base URL (defined in `.c3posettings`) is https://example.com?

This would be a cool feature for sure. But the problem is the implementation. Unless you would also consider something like https://blog.example.com and https://www.blog.example.com the same, finding out the *lower-level domain* (the "example" in example.com, the "foo" in foo.ac.at, sometimes also called *second-level domain*) isn't easy at all. First and foremost, because top-level domains can have two levels as well, such as foo.ac.at. Hence, the common advice is to use Guava's [InternetDomainName](https://github.com/google/guava/blob/ff9fb8d30edbba5357615ecebf69120f1de556f7/android/guava/src/com/google/common/net/InternetDomainName.java) class. It uses generated regular expressions to recognize valid top-level domains. Whenever a new top-level domain gets introduced, these regular expressions need to be adapted and regenerated.

I decided not to support that feature because I didn't want to introduce a big library like Guava for such a small win. Also, it is questionable to reference one's assets through absolute URLs anyways and doing so by using either the non-www or www variant even more so. Just think about changing the domain name one time. You've got to change all those absolute URLs. Use relative URLs instead.

## More Resources
If you want to dive deeper into C-3PO, have a look into the Wiki.
