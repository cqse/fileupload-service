We use [semantic versioning][semver]

# Next version

# 1.0.4

- [fix] add stdout/stderr to timeout error message
- [fix] kill timed out processes
- [fix] increase timeout from 5s to 30s

# 1.0.3

- [breaking change] $F is now {F} when parsing the post upload command
- [feature] allow replacement of {F} anywhere in the command

# 1.0.2

- [fix] invalid multipart handling since logger deserializes request before reading content
- [feature] better logging

# 1.0.1

- [maintenance] add GitLab build
- [feature] additional documentation
- [feature] some more debug logging

# 1.0.0

- initial release


[semver]: http://semver.org/
